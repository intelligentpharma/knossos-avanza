package jobs;

import jobs.comparison.ExperimentLauncher;
import jobs.comparison.FullExperimentLauncher;
import engine.Engine;
import models.ComparisonExperiment;
import models.User;
import static org.easymock.EasyMock.*;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.DBUtils;
import utils.Factory;
import utils.experiment.TestDataCreator;
import utils.queue.PriorityJobQueue;
import utils.queue.PriorityJobQueueItem;

public class FullExperimentLauncherTest extends UnitTest {

    TestDataCreator creator;

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        creator = new TestDataCreator();
    }

    @Test
    public void noExperimentsDeletedLaunch() {
        String username = "aperreau";
        User owner = User.findByUserName(username);
        Factory factory = createMock(Factory.class);
        ComparisonExperiment experiment = creator.getSmallExperiment(owner);
        experiment.save();

        ExperimentLauncher el1 = new FullExperimentLauncher(experiment.id, owner, factory);

        experiment.delete();
        replay(factory);
        el1.doJob();
        verify(factory);
    }

    @Test
    public void experimentLaunchedCorrectly() {
        String username = "aperreau";
        User owner = User.findByUserName(username);
        ComparisonExperiment experiment = creator.getSmallEvaluatedExperiment(owner);
        launchExperimentWithMocks(experiment);

    }

    public static void launchExperimentWithMocks(ComparisonExperiment experiment) {
        String username = "aperreau";
        User owner = User.findByUserName(username);
        PriorityJobQueue priorityJobQueue = createMock(PriorityJobQueue.class);
        priorityJobQueue.add(anyObject(PriorityJobQueueItem.class));

        Engine engine = createMock(Engine.class);
        engine.setExperiment(experiment);
        engine.createAlignments();
        engine.convertDatabases();
        engine.setTargetDeployment(experiment.targetMolecules.getAllDeployments().get(0));
        engine.prepareTargetDeployment();

        DBUtils dbUtils = createMock(DBUtils.class);

        Factory factory = createMock(Factory.class);
        expect(factory.getEngine(experiment)).andReturn(engine).anyTimes();
        expect(factory.getPriorityJobQueue()).andReturn(priorityJobQueue).anyTimes();
        expect(factory.getDBUtils()).andReturn(dbUtils).anyTimes();

        ExperimentLauncher el1 = new FullExperimentLauncher(experiment.id, owner, factory);

        replay(factory, engine, priorityJobQueue);
        el1.doJob();
        verify(factory, engine, priorityJobQueue);
    }
}
