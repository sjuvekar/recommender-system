package edu.umn.cs.recsys.uu;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.data.dao.ItemEventDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.grouplens.lenskit.vectors.similarity.CosineVectorSimilarity;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * User-user item scorer.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SimpleUserUserItemScorer extends AbstractItemScorer {
    private final UserEventDAO userDao;
    private final ItemEventDAO itemDao;

    // This is the vector similarity object used to score two different normalized ratings of two users
    // The object is initialized in the constructor.
    private final CosineVectorSimilarity cosineVectorSimilarity;

    // An integer to indicating the number of neighbors to consider for recommendation
    private final int maxNumberOfNeighbors;

    @Inject
    public SimpleUserUserItemScorer(UserEventDAO udao, ItemEventDAO idao) {
        userDao = udao;
        itemDao = idao;

        // Initialize the Vector Similarity object
        cosineVectorSimilarity = new CosineVectorSimilarity();

        // Set maxNumberOfNeighbors to 30
        maxNumberOfNeighbors = 30;
    }

    @Override
    public void score(long user, @Nonnull MutableSparseVector scores) {
        SparseVector userVector = getUserRatingVector(user);

        // Compute the mean rating by current user
        double userMean = userVector.mean();

        // This is the loop structure to iterate over items to score
        for (VectorEntry e: scores.fast(VectorEntry.State.EITHER)) {
            double score = userMean;
            double numerator = 0.0;
            double denominator = 0.0;

            // First get all users for the current item
            long itemId = e.getKey();

            // Compute the cosine similarities of all other users with current user
            TreeMap<Double, Double> cosineSimilarityMap = getCosineSimilarityMap(userVector, user, itemId);

            // For each of the top 30 neighbors, compute the contribution to the score
            int count = 0;
            for (Map.Entry<Double, Double> neighborEntry : cosineSimilarityMap.entrySet()) {
                double cosineSimilarity = neighborEntry.getKey();
                double adjustedRating = neighborEntry.getValue();

                // Update numerator and denominator
                numerator += cosineSimilarity * adjustedRating;
                denominator += Math.abs(cosineSimilarity);

                // Check if we computed first 300 entries
                count++;
                if (count >= maxNumberOfNeighbors)
                    break;
            }

            // Finally add the score to individual
            score += numerator / denominator;

            // Set the score in the scores vector
            scores.set(itemId, score);
        }
    }

    /**
     * Get a user's rating vector.
     * @param user The user ID.
     * @return The rating vector.
     */
    private SparseVector getUserRatingVector(long user) {
        UserHistory<Rating> history = userDao.getEventsForUser(user, Rating.class);
        if (history == null) {
            history = History.forUser(user);
        }
        return RatingVectorUserHistorySummarizer.makeRatingVector(history);
    }

    /**
     * Get a map of cosine Similarity scores to the adjusted rankings for all users who have rated the item.
     * @param userVector The rating vector of current user. This vector is not guaranteed to be normalized
     * @param user The id of current user. This user should not be inserted in output map.
     * @param itemId The id of the item being rated.
     * @returns A map (cosine similarity with userVector) -> (Adjusted rating for give item) for different users.
     * Keys of this map are cosine similarity scores. A TreeMap helps in sorting those keys in decreasing order.
     */
    private TreeMap<Double, Double> getCosineSimilarityMap(SparseVector userVector, long user, long itemId) {
        // Initialize a TreeMap that holds sorted (cosine similarity with userVector) -> (Adjusted rating for give item)
        TreeMap<Double, Double> similarityScores = new TreeMap<Double, Double>(Collections.reverseOrder());

        //First normalize the userVector
        double userMean = userVector.mean();
        MutableSparseVector normalizedUserVector = userVector.mutableCopy();
        normalizedUserVector.add(-userMean);

        // Next find all users who have rated the given item (use itemDao)
        LongSet allUserIds = itemDao.getUsersForItem(itemId);

        //Iterate over all other users and insert cosine similarity in TreeMap
        assert allUserIds != null;
        for (long otherUserId : allUserIds) {
            // First check if otherUserId is equal to user. In that case, don't insert it into the map
            if (otherUserId == user)
                continue;

            // Get the rating vector for other user
            MutableSparseVector normalizedOtherUserVector = getUserRatingVector(otherUserId).mutableCopy();

            // Normalized the other user's vector
            double otherMean = normalizedOtherUserVector.mean();
            normalizedOtherUserVector.add(-otherMean);

            // Now find the cosine-similarity between normalizedUserVector and normalizedOtherUserVector
            double cosineSimilarity = cosineVectorSimilarity.similarity(normalizedUserVector, normalizedOtherUserVector);

            // Finally, compute the adjusted rating by otherUserId for itemId.
            double adjustedRating = normalizedOtherUserVector.get(itemId);

            // Done, put the cosine_similarity -> adjustedRating to map
            similarityScores.put(cosineSimilarity, adjustedRating);
        }
        return similarityScores;
    }

}
