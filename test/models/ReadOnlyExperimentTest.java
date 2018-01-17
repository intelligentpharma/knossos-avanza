package models;

import engine.Engine;
import engine.tridimensional.docking.FakeInverseDockingEngine;

import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;
import utils.experiment.TestDataCreator;

public class ReadOnlyExperimentTest extends UnitTest {

	private static User owner;
	private static ComparisonExperiment newExperiment, evaluatedExperiment, nxmExperiment, dockingExperiment;
	private static TestDataCreator creator;

	
	@BeforeClass
	public static void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		creator = new TestDataCreator();
		owner = User.findByUserName("xarroyo");
		newExperiment = creator.getSmallExperiment(owner);
		newExperiment.save();
		evaluatedExperiment = creator.getSmallEvaluatedExperiment(owner);
		evaluatedExperiment.status = ExperimentStatus.FINISHED;		
		for(Alignment alignment : evaluatedExperiment.alignments){
			MapsSimilarities similarities = (MapsSimilarities)alignment;
			for(Scoring scoring : similarities.scorings){
				scoring.score = Math.random();
			}
		}
		nxmExperiment = creator.getNxMExperiment(owner);
		nxmExperiment = creator.getNxMEvaluatedExperiment(owner);
		nxmExperiment.save();
		
		dockingExperiment = creator.getSmallEvaluatedDockingExperiment(owner);
		dockingExperiment.save();
		
	}

	@Test
	public void experimentsOwnedByUserFoundCorrectly() {
		User user = User.findByUserName("aperreau");
		List<ComparisonExperiment> experiments = ComparisonExperiment.findAllOwnedBy(user);
		assertEquals(1, experiments.size());
		ComparisonExperiment experiment = experiments.get(0);
		assertEquals("Primera prueba", experiment.name);
		assertEquals("Salts", experiment.targetMolecules.name);
		assertEquals(1, experiment.engineId);
	}

	@Test
	public void addingExperimentWorks() {
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.name = "Test experiment";
		experiment.engineId = Factory.AUTODOCK_VINA;
		experiment.engineName = Factory.AUTODOCK_VINA_NAME;
		experiment.chargeType = Factory.GASTEIGER;
		experiment.probeMolecules = MoleculeDatabase.find("byName", "Proteins").first();
		experiment.targetMolecules = MoleculeDatabase.find("byName", "Salts").first();
		experiment.owner = User.findByUserName("aperreau");
		experiment.save();
		List<ComparisonExperiment> experiments = ComparisonExperiment.findAllOwnedBy(experiment.owner);
		assertEquals(2, experiments.size());
		experiment.delete();
	}

	@Test
	public void addingExperimentWithSingleDatabasesHasOnlyOneSimilarityModel() {
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.name = "Test experiment";
		experiment.engineId = Factory.AUTODOCK_VINA;
		experiment.engineName = Factory.AUTODOCK_VINA_NAME;
		experiment.chargeType = Factory.EEM;
                MoleculeDatabase probeMolecules = creator.createSingleMoleculeDatabase(owner);
		experiment.probeMolecules = probeMolecules;
                MoleculeDatabase targetMolecules = creator.createSingleMoleculeDatabase(owner);
		experiment.targetMolecules = targetMolecules;
		experiment.owner = owner;
		Engine fake = new FakeInverseDockingEngine();
		fake.setExperiment(experiment);
		fake.createAlignments();
		experiment.save();
		assertEquals(1, experiment.alignments.size());
		experiment.delete();
                probeMolecules.delete();
                targetMolecules.delete();
	}
	//TODO: This test is removed because now you can have an experiment with unowned parts.  
//	@Test
	public void addingExperimentWithNotOwnedDataFails() {
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = Factory.AUTODOCK_VINA;
		experiment.probeMolecules = MoleculeDatabase.find("byName", "Sugars").first();
		experiment.targetMolecules = Molecule.find("byName", "Sugars").first();
		User user = User.findByUserName("aperreau");
		experiment.owner = user;
		experiment.save();
		List<ComparisonExperiment> experiments = ComparisonExperiment.findAllOwnedBy(user);
		assertEquals(1, experiments.size());
	}

	@Test
	public void allSimilaritiesAreCreated() {
		assertEquals(7, evaluatedExperiment.alignments.size());
		assertEquals(28, nxmExperiment.alignments.size());
	}
	
	@Test
	public void newExperimentHasQueuedStatus(){
		assertEquals(ExperimentStatus.QUEUED, newExperiment.status);
	}
	
	@Test
	public void evaluatedExperimentHasFinishedStatus() {
		assertEquals(ExperimentStatus.FINISHED, evaluatedExperiment.status);
	}
	
	@Test
	public void bestAlignmentByMoleculePairAndPonderationReturnsList(){
		Ponderation ponderation = Ponderation.getDefaultPonderations().get(0);
		List<Alignment> bestAlignments = evaluatedExperiment.getBestAlignmentsPerMoleculePairAndPonderation(ponderation);
		assertNotNull(bestAlignments);
		assertEquals(evaluatedExperiment.probeMolecules.molecules.size(), bestAlignments.size());
		for(Alignment alignment: bestAlignments){
			assertNotNull(alignment);
			assertNotNull(alignment.id);
		}
	}

	@Test
	public void getBestAlignmentsPerMoleculePairAndPonderation_ForExperimentWithoutScoringsReturnsEmptyList() {
		Ponderation ponderation = Ponderation.getDefaultPonderations().get(0);
		List<Alignment> alignments = newExperiment.getBestAlignmentsPerMoleculePairAndPonderation(ponderation);
		assertTrue(alignments.isEmpty());
	}
	
	@Test
	public void evaluatedInverseDockingExperimentHasPonderations(){
		ComparisonExperiment experiment = evaluatedExperiment;
		List<Ponderation> ponderations = experiment.getPonderations();
		assertEquals(1,ponderations.size());
	}
	
	@Test
	public void experimentLoadedFromYamlWithCorrectCharge(){
		ComparisonExperiment experiment = ComparisonExperiment.findAllOwnedBy(owner).get(0);
		assertEquals(Factory.GASTEIGER, experiment.chargeType);
	}
	
	@Test
	public void seleneIsNotInverseDocking(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = utils.Factory.SELENE;
		assertFalse(experiment.isInverseDocking());
	}
	
	@Test
	public void inverseADIsInverseDocking(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = utils.Factory.AUTODOCK4;
		assertTrue(experiment.isInverseDocking());
	}
	
	@Test
	public void vinaIsInverseDocking(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = utils.Factory.AUTODOCK_VINA;
		assertTrue(experiment.isInverseDocking());
	}
	
	@Test
	public void seleneIsDocking(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = utils.Factory.SELENE;
		assertTrue(experiment.isDocking());
	}
	
	@Test
	public void fakeDockingIsDocking(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = utils.Factory.FAKE_DOCKING;
		assertTrue(experiment.isDocking());
	}
	
	@Test
	public void fakeInverseDockingIsInverseDocking(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = utils.Factory.FAKE_INVERSE_DOCKING;
		assertTrue(experiment.isInverseDocking());
	}
	
	@Test
	public void inverseDockingExperimentReturnsNoPonderations(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = Factory.SELENE;
		assertEquals(0, experiment.getPonderations().size());
	}
	
	@Test
	public void inverseDockingExperimentDoesntRescore(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = Factory.SELENE;
		experiment.rescore(new Ponderation());
		//This would throw an Exception in InverseDocking experiments
	}
	
	@Test
	public void getBestDeploymentsPerMoleculeInInverseDockingExperiment(){
		Ponderation defaultPonderation = Ponderation.getDefaultPonderations().get(0);
		List<Alignment> bestMolecules = evaluatedExperiment.getBestAlignmentsPerMoleculePairAndPonderation(defaultPonderation);
		
		assertEquals(6, bestMolecules.size());
	}
	
	@Test
	public void thereAreFourDifferentStatuses(){
		assertEquals("Queued", ExperimentStatus.QUEUED);
		assertEquals("Running", ExperimentStatus.RUNNING);
		assertEquals("Finished", ExperimentStatus.FINISHED);
		assertEquals("Error", ExperimentStatus.ERROR);
	}
        
        @Test
	public void listAllUsingWorks() {
                MoleculeDatabase probeDatabase = creator.createSingleMoleculeDatabase(owner);
                MoleculeDatabase targetDatabase = creator.createSingleMoleculeDatabase(owner);
                MoleculeDatabase referenceDatabase = creator.createSingleMoleculeDatabase(owner);
		ComparisonExperiment experiment = creator.getSmallExperimentWithGivenDatabases(owner, probeDatabase, targetDatabase, referenceDatabase);		
		List<String> experiments = ComparisonExperiment.findAllUsingIds(probeDatabase);
		assertEquals(1, experiments.size());
                experiments.clear();
                experiments = ComparisonExperiment.findAllUsingIds(targetDatabase);
		assertEquals(1, experiments.size());
                experiments.clear();
                experiments = ComparisonExperiment.findAllUsingIds(referenceDatabase);
		assertEquals(1, experiments.size());
		experiment.delete();
	}
	
}
