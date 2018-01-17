package models;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;

public class PonderationTest extends UnitTest{

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");		
	}

	@Test
	public void inverseADPonderationsSetCorrectly(){
		List<Ponderation> ponderations = Ponderation.getDefaultPonderations();
		assertEquals(1, ponderations.size());
		Ponderation ponderation = ponderations.get(0);
		assertInverseADPonderationWeightsAreCorrect(ponderation);
	}

	public static void assertInverseADPonderationWeightsAreCorrect(Ponderation ponderation) {
		assertEquals(0.0416666, ponderation.getWeights().getMapValue("A"), 0.0);
		assertEquals(0.0104166, ponderation.getWeights().getMapValue("Br"), 0.0);
		assertEquals(0.0416666, ponderation.getWeights().getMapValue("C"), 0.0);
		assertEquals(0.0083333, ponderation.getWeights().getMapValue("Ca"), 0.0);
		assertEquals(0.0104166, ponderation.getWeights().getMapValue("Cl"), 0.0);
		assertEquals(0.0104166, ponderation.getWeights().getMapValue("F"), 0.0);
		assertEquals(0.0083333, ponderation.getWeights().getMapValue("Fe"), 0.0);
		assertEquals(0.0416666, ponderation.getWeights().getMapValue("HD"), 0.0);
		assertEquals(0.0104166, ponderation.getWeights().getMapValue("I"), 0.0);
		assertEquals(0.0083333, ponderation.getWeights().getMapValue("Mg"), 0.0);
		assertEquals(0.0083333, ponderation.getWeights().getMapValue("Mn"), 0.0);
		assertEquals(0.0069444, ponderation.getWeights().getMapValue("N"), 0.0);
		assertEquals(0.0069444, ponderation.getWeights().getMapValue("NA"), 0.0);
		assertEquals(0.0069444, ponderation.getWeights().getMapValue("NS"), 0.0);
		assertEquals(0.0069444, ponderation.getWeights().getMapValue("OA"), 0.0);
		assertEquals(0.0069444, ponderation.getWeights().getMapValue("OS"), 0.0);
		assertEquals(0.0416666, ponderation.getWeights().getMapValue("P"), 0.0);
		assertEquals(0.0416666, ponderation.getWeights().getMapValue("S"), 0.0);
		assertEquals(0.0069444, ponderation.getWeights().getMapValue("SA"), 0.0);
		assertEquals(0.0083333, ponderation.getWeights().getMapValue("Zn"), 0.0);
		assertEquals(0.3333333, ponderation.getWeights().getMapValue("e"), 0.0);
		assertEquals(0.3333333, ponderation.getWeights().getMapValue("d"), 0.0);
	}
	
	@Test
	public void ponderationMustHaveName() {
		try{
			Ponderation ponderation = new Ponderation();
			ponderation.save();
			fail();
		}catch(Exception e){
		}
	}

	@Test
	public void ponderationWithoutSourceExperimentHasSimpleName(){
		Ponderation ponderation = new Ponderation();
		ponderation.name = "Test";
		assertEquals("Test", ponderation.name);
	}
	
	@Test
	public void ponderationWithSourceAndDefaultTrainingType(){
		Ponderation ponderation = new Ponderation();
		ponderation.source = new ComparisonExperiment();
		ponderation.source.name = "Experiment";
		ponderation.name = "Test";
		ponderation.bedroc = 12.1437;
		assertEquals("Experiment_Test_MANUAL (12.144)", ponderation.name);
	}
	
	@Test
	public void ponderationWithSourceAndPholusType() {
		Ponderation ponderation = new Ponderation();
		ponderation.source = new ComparisonExperiment();
		ponderation.source.name = "Experiment";
		ponderation.name = "Test";
		ponderation.bedroc = 12.1437;
		ponderation.trainingType = Factory.PHOLUS;
		assertEquals("Experiment_Test_PHOLUS (12.144)", ponderation.name);
	}
}
