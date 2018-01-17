package jobs;

import jobs.comparison.ExperimentErrorsRelauncher;
import engine.Engine;
import models.Alignment;
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

public class ExperimentErrorsRelauncherTest extends UnitTest {

    TestDataCreator creator;

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        creator = new TestDataCreator();
    }

    @Test
    public void deletedExperimentsAreNotRelaunched() {
        String username = "aperreau";
        User owner = User.findByUserName(username);
        Factory factory = createMock(Factory.class);
        ComparisonExperiment experiment = creator.getSmallExperiment(owner);
        experiment.save();

        ExperimentErrorsRelauncher el1 = new ExperimentErrorsRelauncher(experiment.id, owner, factory);

        experiment.delete();
        replay(factory);
        el1.doJob();
        verify(factory);
    }

    @Test
    public void errorsRelaunchedCorrectly() {
        String username = "aperreau";
        User owner = User.findByUserName(username);
        ComparisonExperiment experiment = creator.getSmallEvaluatedExperiment(owner);
        experiment.alignments.get(0).error = true;
        experiment.alignments.get(2).error = true;
        experiment.save();
        relaunchExperimentWithMocks(experiment);
    }

    public void relaunchExperimentWithMocks(ComparisonExperiment experiment) {
        String username = "aperreau";
        User owner = User.findByUserName(username);
        Factory factory = createMock(Factory.class);
        Engine engine = createMock(Engine.class);
        DBUtils dbUtils = createMock(DBUtils.class);
        PriorityJobQueue priorityJobQueue = createMock(PriorityJobQueue.class);
        priorityJobQueue.add(anyObject(PriorityJobQueueItem.class));
        engine.setExperiment(experiment);
        expect(factory.getEngine(experiment)).andReturn(engine).anyTimes();
        expect(factory.getPriorityJobQueue()).andReturn(priorityJobQueue).anyTimes();
        expect(factory.getDBUtils()).andReturn(dbUtils).anyTimes();
        engine.setTargetDeployment(experiment.targetMolecules.getAllDeployments().get(0));
        engine.prepareTargetDeployment();

        ExperimentErrorsRelauncher el1 = new ExperimentErrorsRelauncher(experiment.id, owner, factory);

        replay(factory, engine, priorityJobQueue);
        el1.doJob();
        verify(factory, engine, priorityJobQueue);
    }

    @Test
    public void relaunchingSetsErrorInAlignmentToFalse() {
        String username = "aperreau";
        User owner = User.findByUserName(username);
        ComparisonExperiment experiment = creator.getSmallEvaluatedExperiment(owner);
        Alignment alignment1 = experiment.alignments.get(0);
        alignment1.error = true;
        Alignment alignment2 = experiment.alignments.get(2);
        alignment2.error = true;
        experiment.save();
        relaunchExperimentWithMocks(experiment);

        assertFalse(alignment1.error);
        assertFalse(alignment2.error);
    }

    @Test
    public void relaunchingSetsFinishedInAlignmentToFalse() {
        String username = "aperreau";
        User owner = User.findByUserName(username);
        ComparisonExperiment experiment = creator.getSmallEvaluatedExperiment(owner);
        Alignment alignment1 = experiment.alignments.get(0);
        alignment1.error = true;
        Alignment alignment2 = experiment.alignments.get(2);
        alignment2.error = true;
        experiment.save();
        relaunchExperimentWithMocks(experiment);

        assertFalse(alignment1.finished);
        assertFalse(alignment2.finished);
    }
}
