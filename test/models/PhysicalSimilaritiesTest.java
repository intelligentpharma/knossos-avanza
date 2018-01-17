package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.experiment.TestDataCreator;

public class PhysicalSimilaritiesTest extends UnitTest {

	ComparisonExperiment experiment;
	Ponderation ponderation;
	
	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		TestDataCreator dataCreator = new TestDataCreator();
		User owner = User.findByUserName("aperreau");
		experiment = dataCreator.getSmallEvaluatedDockingExperiment(owner);
		ponderation = null;

		experiment.save();
	}
	@Test
	public void getsCorrectMoleculePair() {
		PhysicalSimilarities similarities = new PhysicalSimilarities();
		similarities.targetDeployment = new Deployment();
		Molecule probe = new Molecule();
		similarities.targetDeployment.molecule = probe;
		similarities.probeDeployment = new Deployment();
		Molecule target = new Molecule();
		similarities.probeDeployment.molecule = target;
		MoleculePair pair = new MoleculePair(target,probe);
		assertEquals(pair, similarities.getMoleculePair());
	}

	@Test
	public void comparisionWithItselfIsZero(){
		PhysicalSimilarities similarities = new PhysicalSimilarities();
		similarities.energy= 2.3;
		assertEquals(0,similarities.compare(similarities, ponderation),0.001);
	}
	
	@Test
	public void comparisionResultIsCorrect(){
		PhysicalSimilarities similarities1 = new PhysicalSimilarities();
		similarities1.energy= 1;
		PhysicalSimilarities similarities2 = new PhysicalSimilarities();
		similarities2.energy= 2;
		assertEquals(1,similarities1.compare(similarities2, ponderation),0.001);	
		assertEquals(-similarities2.compare(similarities1, ponderation),similarities1.compare(similarities2, ponderation),0.001);
	}
	
	@Test 
	public void getAllPhysicalSimilaritiesFromExperiment(){
		Molecule target = experiment.targetMolecules.molecules.get(0);
		Molecule probe = experiment.probeMolecules.molecules.get(5);
		List<PhysicalSimilarities>  similarities = PhysicalSimilarities.findByExperimentMoleculePairAndEnergy(experiment, target, probe);
		assertEquals(2, similarities.size());
	}
	
	@Test
	public void getBetsDeploymentPerMoleculeInDockingExperiment(){
		List<PhysicalSimilarities> bestMolecules = new ArrayList<PhysicalSimilarities>();
		generateRandomEnergies();
		
		for(Molecule target : experiment.targetMolecules.molecules){
			for(Molecule probe : experiment.probeMolecules.molecules){
				List<PhysicalSimilarities> similarities = PhysicalSimilarities.findByExperimentMoleculePairAndEnergy(experiment, target, probe);
				PhysicalSimilarities bestSimilarity = PhysicalSimilarities.findBestByExperimentMoleculePairAndEnergy(experiment, target, probe);
				for(PhysicalSimilarities similarity : similarities){
Logger.info("best similarity has lower value " + bestSimilarity.energy + "<" + similarity.energy);
					assertTrue(bestSimilarity.energy <= similarity.energy);
				}
				bestMolecules.add(bestSimilarity);
			}
		}
		assertEquals(6, bestMolecules.size());
	}
	private void generateRandomEnergies() {
		for(Alignment alignment : experiment.alignments){
			PhysicalSimilarities similarities = (PhysicalSimilarities)alignment;
			similarities.energy = (new Random()).nextDouble() * -10;
			similarities.save();
		}
	}
}
