package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.experiment.TestDataCreator;

public class ExperimentTest extends UnitTest {

    User owner;
    TestDataCreator dataCreator;

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        owner = User.findByUserName("aperreau");
        dataCreator = new TestDataCreator();
    }

    @Test
    public void experimentWithSomeEvaluatedHasRunningStatus() {
        ComparisonExperiment experiment = dataCreator.getSmallEvaluatedExperiment(owner);
        experiment.alignments.get(0).finished = false;
        experiment.status = ExperimentStatus.RUNNING;
        experiment.save();
        assertEquals(ExperimentStatus.RUNNING, experiment.status);
    }

    @Test
    public void experimentWithSomeFinishedAlignmentsHasCorrectProgress() {
        ComparisonExperiment experiment = dataCreator.getSmallEvaluatedExperiment(owner);
        experiment.alignments.get(0).finished = false;
        experiment.status = ExperimentStatus.RUNNING;
        experiment.save();
        assertEquals("0/6/7", experiment.getProgress());
    }

    @Test
    public void experimentWithSomeErroneousAlignmentsHasCorrectProgress() {
        ComparisonExperiment experiment = dataCreator.getSmallEvaluatedExperiment(owner);
        experiment.alignments.get(0).error = true;
        experiment.status = ExperimentStatus.FINISHED;
        experiment.save();
        assertEquals("1/7/7", experiment.getProgress());
    }

    @Test
    public void removeExperiment() {
        String username = "aperreau";
        User user = User.findByUserName(username);
        List<ComparisonExperiment> ExperimentList = ComparisonExperiment.findAllOwnedBy(user);
        ComparisonExperiment experiment = ExperimentList.get(0);
        experiment.delete();
        assertNull(ComparisonExperiment.findById(experiment.id));
    }

    @Test
    public void rescoring() {
        ComparisonExperiment experiment = dataCreator.getSmallEvaluatedExperiment(owner);
        Ponderation ponderation = new Ponderation();
        ponderation.name = "ponderation1";
        ponderation.owner = User.findByUserName(owner.username);
        Set<String> mapNames = PhysicalMaps.getMapNames();
        for (String name : mapNames) {
            ponderation.weights.setMapValue(name, Math.random());
        }
        assertEquals(1, ((MapsSimilarities) experiment.alignments.get(0)).scorings.size());
        experiment.rescore(ponderation);
        assertEquals(2, ((MapsSimilarities) experiment.alignments.get(0)).scorings.size());
    }

    @Test
    public void getPonderationsUsedByExperiment() {
        ComparisonExperiment experiment1 = dataCreator.getSmallEvaluatedExperiment(owner);
        Ponderation ponderation1 = new Ponderation();
        ponderation1.name = "ponderation1";
        ponderation1.owner = User.findByUserName(owner.username);
        Set<String> mapNames = PhysicalMaps.getMapNames();
        for (String name : mapNames) {
            ponderation1.weights.setMapValue(name, Math.random());
        }
        experiment1.rescore(ponderation1);

        ComparisonExperiment experiment2 = dataCreator.getSmallEvaluatedExperiment(owner);
        Ponderation ponderation2 = new Ponderation();
        ponderation2.name = "ponderation2";
        ponderation2.owner = User.findByUserName(owner.username);
        for (String name : mapNames) {
            ponderation2.weights.setMapValue(name, Math.random());
        }
        experiment2.rescore(ponderation2);

        List<Ponderation> ponderations1 = experiment1.getPonderations();
        assertEquals(2, ponderations1.size());
        assertEquals("ponderation1", ponderations1.get(1).name);
        List<Ponderation> ponderations2 = experiment2.getPonderations();
        assertEquals(2, ponderations2.size());
        assertEquals("ponderation2", ponderations2.get(1).name);

    }

    @Test
    public void experimentReturnsNumberOfFinishedAligmnmentsWithErrorCorrectly() {
        ComparisonExperiment experiment = dataCreator.getSmallEvaluatedExperiment(owner);
        experiment.alignments.get(0).finished = false;
        experiment.alignments.get(0).error = true;
        experiment.alignments.get(1).error = true;
        experiment.alignments.get(2).error = true;
        experiment.save();
        assertEquals(2, experiment.getNumberOfFinishedAlignmentsWithError());
    }

    @Test
    public void getsAlignmentsWithErrors() {
        ComparisonExperiment experiment = dataCreator.getSmallEvaluatedExperiment(owner);
        experiment.alignments.get(0).finished = false;
        experiment.alignments.get(0).error = true;
        experiment.alignments.get(1).error = true;
        experiment.alignments.get(2).error = true;
        experiment.save();
        List<Alignment> errorAlignments = experiment.getAlignmentsWithError();
        assertEquals(2, errorAlignments.size());
        assertTrue(errorAlignments.get(0).id == experiment.alignments.get(1).id);
        assertTrue(errorAlignments.get(1).id == experiment.alignments.get(2).id);
    }

    @Test
    public void removeDockingExperiment() {
        String username = "aperreau";
        User user = User.findByUserName(username);
        ComparisonExperiment experiment = dataCreator.getSmallEvaluatedDockingExperiment(user);
        experiment.delete();
        assertNull(ComparisonExperiment.findById(experiment.id));
    }

    @Test
    public void findOnlyExperimentsNotBeingErased() {
        ComparisonExperiment experiment1 = dataCreator.createExperiment();
        ComparisonExperiment experiment2 = dataCreator.createExperiment();

        experiment1.beingErased = true;
        experiment1.save();

        List<ComparisonExperiment> experiments = ComparisonExperiment.findAllOwnedBy(experiment1.owner);

        assertTrue(experiments.contains(experiment2));
        assertFalse(experiments.contains(experiment1));
    }

    @Test
    public void isFinishedWhenAllAlignmentsAreFinished() {
        ComparisonExperiment experiment = dataCreator.getSmallEvaluatedExperiment(owner);
        assertTrue(experiment.isFinished());
    }

    @Test
    public void isNotFinishedWhenSomeAlignmentsAreNotFinished() {
        ComparisonExperiment experiment = dataCreator.getSmallEvaluatedExperiment(owner);
        experiment.alignments.get(0).finished = false;
        experiment.alignments.get(1).finished = false;
        experiment.save();

        assertFalse(experiment.isFinished());
    }

    @Test
    public void isRunning() {
        ComparisonExperiment experiment = dataCreator.getSmallExperiment(owner);
        experiment.status = ExperimentStatus.RUNNING;
        assertTrue(experiment.isRunning());
        experiment.status = ExperimentStatus.FINISHED;
        assertFalse(experiment.isRunning());
        experiment.status = ExperimentStatus.QUEUED;
        assertFalse(experiment.isRunning());
    }

    @Test
    public void isTypeOfObjectCorrect() {
        ComparisonExperiment experiment = new ComparisonExperiment();
        assertEquals(experiment.getType(), "experiment");
    }
    
    @Test
    public void getPharmacophoresOfExperiment(){
    	ComparisonExperiment experiment = dataCreator.getSmallEvaluatedExperiment(owner);
		MoleculeDatabase database = MoleculeDatabase.findAllOwnedBy(owner).get(0);
		Pharmacophore pharmacophore1 = new PharmacophoreKnossos(database, 1.0);
		Pharmacophore pharmacophore2 = new PharmacophoreKnossos(database, 2.0);
		pharmacophore1.save();
		pharmacophore2.save();
		
		Alignment similarities1 = (MapsSimilarities)experiment.alignments.get(0);
		similarities1.pharmacophoreScorings = new ArrayList<PharmacophoreScoring>();
		PharmacophoreScoring scoring11 = new PharmacophoreScoring(similarities1, pharmacophore2);
		scoring11.setScore(0.3);
		similarities1.pharmacophoreScorings.add(scoring11);
		PharmacophoreScoring scoring111 = new PharmacophoreScoring(similarities1, pharmacophore1);
		scoring111.setScore(0.2);
		similarities1.pharmacophoreScorings.add(scoring111);

		Alignment similarities2 = (MapsSimilarities)experiment.alignments.get(1);
		similarities2.pharmacophoreScorings = new ArrayList<PharmacophoreScoring>();
		PharmacophoreScoring scoring21 = new PharmacophoreScoring(similarities2, pharmacophore2);
		scoring21.setScore(0.1);

		PharmacophoreScoring scoring211 = new PharmacophoreScoring(similarities2, pharmacophore1);
		scoring211.setScore(0.05);
		similarities2.pharmacophoreScorings.add(scoring211);

		experiment.save();

		List<Pharmacophore> pharmacophores = experiment.getPharmacophores();
		assertEquals(2, pharmacophores.size());
		
    }
}
