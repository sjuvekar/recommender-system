package org.grouplens.mooc.cbf;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.scored.ScoredId;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class UnweightedProfileRecommenderTest {

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

		assertRecommendation(recs.get(0), 11, 0.3596);
		assertRecommendation(recs.get(1), 63, 0.2612);
		assertRecommendation(recs.get(2), 807, 0.2363);
		assertRecommendation(recs.get(3), 187, 0.2059);
		assertRecommendation(recs.get(4), 2164, 0.1899);
	}
	
	@Test
	public void shouldReturnRecommendationsForExample2Correctly() {
		List<ScoredId> recs = CBFMain.getRecommendationsFor(
				Long.parseLong(testUsers[1]), 5);
		  
		assertRecommendation(recs.get(0), 11, 0.3715);
		assertRecommendation(recs.get(1), 585, 0.2512);
		assertRecommendation(recs.get(2), 38, 0.1908);
		assertRecommendation(recs.get(3), 141, 0.1861);
		assertRecommendation(recs.get(4), 807, 0.1748);
	}
	
	@Test
	public void shouldReturnRecommendationsForExample3Correctly() {
		List<ScoredId> recs = CBFMain.getRecommendationsFor(
				Long.parseLong(testUsers[2]), 5);
		assertRecommendation(recs.get(0), 1892, 0.4303);
		assertRecommendation(recs.get(1), 1894, 0.2958);
		assertRecommendation(recs.get(2), 63, 0.2226);
		assertRecommendation(recs.get(3), 2164, 0.2119);
		assertRecommendation(recs.get(4), 604, 0.1941);
	}
	
	@Test
	public void shouldReturnRecommendationsForExample4Correctly() {
		List<ScoredId> recs = CBFMain.getRecommendationsFor(
				Long.parseLong(testUsers[3]), 5);
		assertRecommendation(recs.get(0), 2164, 0.2272);
		assertRecommendation(recs.get(1), 141, 0.2225);
		assertRecommendation(recs.get(2), 745, 0.2067);
		assertRecommendation(recs.get(3), 601, 0.1995);
		assertRecommendation(recs.get(4), 807, 0.1846);
	}
	
	@Test
	public void shouldReturnRecommendationsForExample5Correctly() {
		List<ScoredId> recs = CBFMain.getRecommendationsFor(
				Long.parseLong(testUsers[4]), 5);
		assertRecommendation(recs.get(0), 11, 0.3659);
		assertRecommendation(recs.get(1), 1891, 0.3278);
		assertRecommendation(recs.get(2), 640, 0.1958);
		assertRecommendation(recs.get(3), 424, 0.1840);
		assertRecommendation(recs.get(4), 180, 0.1527);
	}

	private void assertRecommendation(ScoredId item, int id, double score) {
		assertEquals(item.getId(), id);
		assertTrue(Math.abs(score - item.getScore()) < 0.0001);
	}
	
}
