package utils;

import java.util.List;

import jobs.comparison.ExperimentErrorsRelauncher;
import models.*;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.DBUtilsImpl;
import utils.Factory;
import utils.KnossosInitializerImpl;
import utils.experiment.TestDataCreator;

public class KnossosInitializerTest extends UnitTest {

    KnossosInitializerImpl initializer;
    Factory factory;
    TestDataCreator creator;

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        initializer = new KnossosInitializerImpl();
        initializer.setDbUtils(new DBUtilsImpl());
        creator = new TestDataCreator();
        factory = EasyMock.createMock(Factory.class);
        initializer.setFactory(factory);


    }

    @Test
    public void returnsEmptyListIfThereAreNoExperimentsToInitialize() {
        Fixtures.deleteDatabase();
        assertEquals(0, initializer.getExperimentsToInitialize().size());
    }

    @Test
    public void returnsCorrectListOfExperimentsToInitialize() {
        User owner = User.findByUserName("aperreau");
        ComparisonExperiment experiment = creator.getSmallEvaluatedDockingExperiment(owner);
        experiment.status = ExperimentStatus.RUNNING;
        experiment.save();

        List<Experiment> experiments = initializer.getExperimentsToInitialize();

        assertEquals(2, experiments.size());
    }

    @Test
    public void listWithExperimentsToInitializeIsCorrectlyOrderedByStatus() {
        User owner = User.findByUserName("aperreau");
        ComparisonExperiment experiment = creator.getSmallEvaluatedDockingExperiment(owner);
        experiment.status = ExperimentStatus.RUNNING;
        experiment.save();

        ComparisonExperiment experiment2 = creator.getSmallEvaluatedDockingExperiment(owner);
        experiment2.status = ExperimentStatus.QUEUED;
        experiment2.save();

        List<Experiment> experiments = initializer.getExperimentsToInitialize();

        assertEquals(ExperimentStatus.RUNNING, experiments.get(0).status);
        assertEquals(ExperimentStatus.QUEUED, experiments.get(1).status);
        assertEquals(ExperimentStatus.QUEUED, experiments.get(2).status);
    }

    @Test
    public void afterInitializationThereAreNoQueuedOrRunningExperiments() {
        List<Experiment> experiments = initializer.getExperimentsToInitialize();

        initializer.initializeExperiments();

        //Needed because the status are updated behind the scenes (of the hibernate movie)
        for (Experiment experiment : experiments) {
            experiment.refresh();
        }

        assertEquals(0, initializer.getExperimentsToInitialize().size());
    }

    @Test
    public void afterInitializationAllInitializedExperimentsArePreparedToBeRelaunched() {
        List<Experiment> experiments = initializer.getExperimentsToInitialize();

        initializer.initializeExperiments();

        for (Experiment experiment : experiments) {
            experiment.refresh();
            assertEquals(ExperimentStatus.FINISHED, experiment.status);
            for (Alignment alignment : ((ComparisonExperiment) experiment).alignments) {
                assertTrue(alignment.finished);
            }
        }
    }

    @Test
    public void afterRelaunchingAllInitializedExperimentsAreQueued() {
        
        List<Experiment> experiments = initializer.getExperimentsToInitialize();

        ExperimentErrorsRelauncher relauncher = EasyMock.createMock(ExperimentErrorsRelauncher.class);
       
        factory.setUsername("aperreau");
        EasyMock.expect(factory.createExperimentErrorsRelauncher(EasyMock.anyLong(), EasyMock.eq(User.findByUserName("aperreau")))).andReturn(relauncher).times(experiments.size());
        EasyMock.expect(relauncher.now()).andReturn(null).times(experiments.size());

        EasyMock.replay(factory, relauncher);

        initializer.initializeAndRelaunchExperiments();

        for (Experiment experiment : experiments) {
            assertEquals(experiment.status, ExperimentStatus.QUEUED);
        }
    }

    @Test
    public void aRelauncherJobIsLaunchedForEachInitializedExperiment() {
        
        List<Experiment> experiments = initializer.getExperimentsToInitialize();

        factory.setUsername("aperreau");
        ExperimentErrorsRelauncher relauncher = EasyMock.createMock(ExperimentErrorsRelauncher.class);
        EasyMock.expect(factory.createExperimentErrorsRelauncher(EasyMock.anyLong(), EasyMock.eq(User.findByUserName("aperreau")))).andReturn(relauncher).times(experiments.size());
        EasyMock.expect(relauncher.now()).andReturn(null).times(experiments.size());


        EasyMock.replay(factory, relauncher);

        initializer.initializeAndRelaunchExperiments();

        EasyMock.verify(factory, relauncher);
    }
}