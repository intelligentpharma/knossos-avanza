package utils.bidimensional;

import org.junit.Test;

import play.test.UnitTest;
import utils.bidimensional.Smile;
import utils.bidimensional.SmilesComparisonResult;

public class SmilesComparisonResultsTest extends UnitTest {

	@Test
	public void calculatesOrComparison(){
		Smile target = new Smile("COCOCO","mol1");
		Smile probe = new Smile("COCOCOH","mol2");
		SmilesComparisonResult results = new SmilesComparisonResult(target, probe);
		
		assertEquals(0.6667, results.orComparison, 0.001);
	}

	@Test
	public void calculatesTargetComparison(){
		Smile target = new Smile("COCOCO","mol1");
		Smile probe = new Smile("COCOCOH","mol2");
		SmilesComparisonResult results = new SmilesComparisonResult(target, probe);
		
		assertEquals(1.000, results.targetComparison, 0.001);
	}

	@Test
	public void calculatesProbeComparison(){
		Smile target = new Smile("COCOCO","mol1");
		Smile probe = new Smile("COCOCOH","mol2");
		SmilesComparisonResult results = new SmilesComparisonResult(target, probe);
		
		assertEquals(0.6667, results.probeComparison, 0.001);
	}
	
	@Test
	public void betterSimilarityIsOrderedFirst(){
		Smile target = new Smile("COCOCO","mol1");
		Smile probe = new Smile("COCOCOH","mol2");
		SmilesComparisonResult worseResults = new SmilesComparisonResult(target, probe);
		SmilesComparisonResult bestResults = new SmilesComparisonResult(target, target);
		
		assertEquals(-1, bestResults.compareTo(worseResults));
	}
	
	@Test
	public void printsCorrectlyToCsvFormat(){
		Smile target = new Smile("COCOCO","mol1");
		Smile probe = new Smile("COCOCOH","mol2");
		SmilesComparisonResult results = new SmilesComparisonResult(target, probe);
		
		assertEquals("COCOCO,mol1,COCOCOH,mol2,0.667,1.000,0.667", results.toCsv());
	}
	
	@Test
	public void tanimotoCorrectXAMolecules(){
		Smile target = new Smile("S(c1n(c2CCCc2c(=O)n1)CC(=O)N(CCN(CC)CC)Cc1ccc(cc1)c1ccc(cc1)C(F)(F)F)Cc1ccc(F)cc1","mol1");
		Smile probe = new Smile("O(c1c2c(n(c(c2C(=O)C(=O)N)CC)Cc2ccccc2)ccc1)CC(=O)O","mol2");
		SmilesComparisonResult results = new SmilesComparisonResult(target, probe);
		
		assertEquals("S(c1n(c2CCCc2c(=O)n1)CC(=O)N(CCN(CC)CC)Cc1ccc(cc1)c1ccc(cc1)C(F)(F)F)Cc1ccc(F)cc1,mol1,O(c1c2c(n(c(c2C(=O)C(=O)N)CC)Cc2ccccc2)ccc1)CC(=O)O,mol2,0.178,0.244,0.369", results.toCsv());
	}
}
