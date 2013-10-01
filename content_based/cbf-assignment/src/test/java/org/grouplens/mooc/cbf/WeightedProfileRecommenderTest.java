package org.grouplens.mooc.cbf;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.scored.ScoredId;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class WeightedProfileRecommenderTest {

	private String[] testUsers;

	@BeforeTest
	public void getRecommendations() throws RecommenderBuildException {
		String[] testUsers = { "4045", "144", "3855", "1637", "2919" };
		this.testUsers = testUsers;

		CBFMain.main(testUsers);
	}

	@Test
	public void shouldReturnRecommendationsForExample1Correctly() {
		List<ScoredId> recs = CBFMain.getRecommendationsFor(
				Long.parseLong(testUsers[0]), 5);
		assertRecommendation(recs.get(0), 807, 0.1932);
		assertRecommendation(recs.get(1), 63, 0.1438);
		assertRecommendation(recs.get(2), 187, 0.0947);
		assertRecommendation(recs.get(3), 11, 0.0900);
		assertRecommendation(recs.get(4), 641, 0.0471);
	}
	
	@Test
	public void shouldReturnRecommendationsForExample2Correctly() {
		List<ScoredId> recs = CBFMain.getRecommendationsFor(
				Long.parseLong(testUsers[1]), 5);
		assertRecommendation(recs.get(0), 11, 0.1394);
		assertRecommendation(recs.get(1), 585, 0.1229);
		assertRecommendation(recs.get(2), 671, 0.1130);
		assertRecommendation(recs.get(3), 672, 0.0878);
		assertRecommendation(recs.get(4), 141, 0.0436);
	}
	
	@Test
	public void shouldReturnRecommendationsForExample3Correctly() {
		List<ScoredId> recs = CBFMain.getRecommendationsFor(
				Long.parseLong(testUsers[2]), 5);
		assertRecommendation(recs.get(0), 1892, 0.2243);
		assertRecommendation(recs.get(1), 1894, 0.1465);
		assertRecommendation(recs.get(2), 604, 0.1258);
		assertRecommendation(recs.get(3), 462, 0.1050);
		assertRecommendation(recs.get(4), 10020, 0.0898);
	}
	
	@Test
	public void shouldReturnRecommendationsForExample4Correctly() {
		List<ScoredId> recs = CBFMain.getRecommendationsFor(
				Long.parseLong(testUsers[3]), 5);
		assertRecommendation(recs.get(0), 393, 0.1976);
		assertRecommendation(recs.get(1), 24, 0.1900);
		assertRecommendation(recs.get(2), 2164, 0.1522);
		assertRecommendation(recs.get(3), 601, 0.1334);
		assertRecommendation(recs.get(4), 5503, 0.0992);
	}
	
	@Test
	public void shouldReturnRecommendationsForExample5Correctly() {
		List<ScoredId> recs = CBFMain.getRecommendationsFor(
				Long.parseLong(testUsers[4]), 5);
		assertRecommendation(recs.get(0), 180, 0.1454);
		assertRecommendation(recs.get(1), 11, 0.1238);
		assertRecommendation(recs.get(2), 1891, 0.1172);
		assertRecommendation(recs.get(3), 424, 0.1074);
		assertRecommendation(recs.get(4), 2501, 0.0973);
	}

	private void assertRecommendation(ScoredId item, int id, double score) {
		assertEquals(item.getId(), id);
		assertTrue(Math.abs(score - item.getScore()) < 0.0001);
	}
	
}
