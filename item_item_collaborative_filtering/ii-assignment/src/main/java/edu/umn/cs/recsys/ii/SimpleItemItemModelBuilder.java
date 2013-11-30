package edu.umn.cs.recsys.ii;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.ItemDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.scored.ScoredIdListBuilder;
import org.grouplens.lenskit.scored.ScoredIds;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.grouplens.lenskit.vectors.similarity.CosineVectorSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */

class ScoredIdComparator implements Comparator<ScoredId> {
    public int compare(ScoredId one, ScoredId two) {
        if (one.getScore() > two.getScore()) return -1;
        if (one.getScore() == two.getScore()) return 0;
        return 1;
    }
}

public class SimpleItemItemModelBuilder implements Provider<SimpleItemItemModel> {
    private final ItemDAO itemDao;
    private final UserEventDAO userEventDao;
    private static final Logger logger = LoggerFactory.getLogger(SimpleItemItemModelBuilder.class);
    private CosineVectorSimilarity cosineVectorSimilarity;
    private ScoredIdComparator scoredIdComparator;

    @Inject
    public SimpleItemItemModelBuilder(@Transient ItemDAO idao,
                                      @Transient UserEventDAO uedao) {
        itemDao = idao;
        userEventDao = uedao;
        cosineVectorSimilarity = new CosineVectorSimilarity();
        scoredIdComparator = new ScoredIdComparator();
    }

    @Override
    public SimpleItemItemModel get() {
        // Get the transposed rating matrix
        // This gives us a map of item IDs to those items' rating vectors
        Map<Long, ImmutableSparseVector> itemVectors = getItemVectors();

        // Get all items - you might find this useful
        LongSortedSet items = LongUtils.packedSet(itemVectors.keySet());
        // Map items to vectors of item similarities
        Map<Long, ScoredIdListBuilder> itemSimilarities = new HashMap<Long, ScoredIdListBuilder>();

        // It will need to be in a map of longs to lists of Scored IDs to store in the model
        for (long firstItem: items) {
            for (long secondItem: items) {
                if (firstItem == secondItem) continue;
                ImmutableSparseVector firstRatings = itemVectors.get(firstItem);
                ImmutableSparseVector secondRatings = itemVectors.get(secondItem);
                double similarity = cosineVectorSimilarity.similarity(firstRatings, secondRatings);
                if (similarity <= 0) continue;
                ScoredIdListBuilder currSimilarityList = null;
                if (itemSimilarities.containsKey(firstItem))
                    currSimilarityList = itemSimilarities.get(firstItem);
                else
                    currSimilarityList = ScoredIds.newListBuilder();
                currSimilarityList.add(secondItem, similarity);
                itemSimilarities.put(firstItem, currSimilarityList);
            }
        }

        // Done building the map. Now turn every entry of the map into a ScoredId List.
        Map<Long, List<ScoredId>> itemSimilarityList = new HashMap<Long, List<ScoredId>>();
        for (Long itemId: itemSimilarities.keySet()) {
            ScoredIdListBuilder currSimilarityList = itemSimilarities.get(itemId);
            currSimilarityList.sort(scoredIdComparator);
            itemSimilarityList.put(itemId, currSimilarityList.build());
        }

        return new SimpleItemItemModel(itemSimilarityList);
    }

    /**
     * Load the data into memory, indexed by item.
     * @return A map from item IDs to item rating vectors. Each vector contains users' ratings for
     * the item, keyed by user ID.
     */
    public Map<Long,ImmutableSparseVector> getItemVectors() {
        // set up storage for building each item's rating vector
        LongSet items = itemDao.getItemIds();
        // map items to maps from users to ratings
        Map<Long,Map<Long,Double>> itemData = new HashMap<Long, Map<Long, Double>>();
        for (long item: items) {
            itemData.put(item, new HashMap<Long, Double>());
        }
        // itemData should now contain a map to accumulate the ratings of each item

        // stream over all user events
        Cursor<UserHistory<Event>> stream = userEventDao.streamEventsByUser();
        try {
            for (UserHistory<Event> evt: stream) {
                long userId = evt.getUserId();
                MutableSparseVector vector = RatingVectorUserHistorySummarizer.makeRatingVector(evt).mutableCopy();
                // vector is now the user's rating vector
                double mean = vector.mean();
                vector.add(-mean);
                for (VectorEntry e: vector.fast(VectorEntry.State.EITHER)) {
                    long itemId = e.getKey();
                    Map<Long, Double> itemDataMapForItem = itemData.get(itemId);
                    itemDataMapForItem.put(userId, e.getValue());
                    itemData.put(itemId, itemDataMapForItem);
                }
            }
        } finally {
            stream.close();
        }

        // This loop converts our temporary item storage to a map of item vectors
        Map<Long,ImmutableSparseVector> itemVectors = new HashMap<Long, ImmutableSparseVector>();
        for (Map.Entry<Long,Map<Long,Double>> entry: itemData.entrySet()) {
            MutableSparseVector vec = MutableSparseVector.create(entry.getValue());
            itemVectors.put(entry.getKey(), vec.immutable());
        }
        return itemVectors;
    }
}
