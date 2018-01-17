package jobs;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.matches;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import jobs.comparison.SimilarityCalculation;
import models.Alignment;
import models.ComparisonExperiment;
import models.MapsSimilarities;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.DBUtils;
import utils.Factory;
import utils.experiment.TestDataCreator;
import utils.queue.PriorityJobQueue;
import utils.scripts.SlurmExternalScript;
import engine.Engine;
import models.Experiment;
import models.ExperimentStatus;

public class SimilarityCalculationTest extends UnitTest {

	TestDataCreator creator;
	Factory factory;
	String username; 
	PriorityJobQueue priorityJobQueue;	
	User owner;
	DBUtils dbUtils;
	
	@Before
	public void setUp(){
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		creator = new TestDataCreator();
		factory = createMock(Factory.class);
		priorityJobQueue = createMock(PriorityJobQueue.class);
		expect(factory.getPriorityJobQueue()).andReturn(priorityJobQueue);
		dbUtils = createMock(DBUtils.class);
		expect(factory.getDBUtils()).andReturn(dbUtils);
		username = "xmaresma";
		owner = User.findByUserName(username);
	}
	
	
	@Test
	public void similarityCalculationIsNotLaunchedIfExperimentIsDeleted(){
		ComparisonExperiment experiment = creator.getSmallEvaluatedExperiment(owner);
		experiment.save();

		replay(factory);

		SimilarityCalculation sc1 = new SimilarityCalculation(experiment.alignments.get(0), factory);
		experiment.delete();
		sc1.doJob();

		verify(factory);
	}
	
	@Test
	public void nonExistenSimilarityCalculationLaunch(){
		MapsSimilarities alignment = new MapsSimilarities();
		alignment.id = -1L;

		replay(factory);

		SimilarityCalculation sc1 = new SimilarityCalculation(alignment, factory);
		sc1.doJob();

		verify(factory);
	}
	
	@Test
	public void lastSimilarityCalculationLaunch(){
		ComparisonExperiment experiment = creator.getSmallEvaluatedExperiment(owner);
		Alignment alignment = experiment.alignments.get(0);
		alignment.runTime = 1000;
		experiment.save();
		
		Engine engine = createMock(Engine.class);
		engine.calculate(alignment, SlurmExternalScript.DEFAULT_PARTITION);
		expect(factory.getEngine(experiment)).andReturn(engine);
		expect(factory.getPriorityJobQueue()).andReturn(priorityJobQueue);
		expect(factory.getExternalScriptLauncher()).andReturn(new SlurmExternalScript()).anyTimes();
		String query = "UPDATE COMPARISONEXPERIMENT set status='Finished', endDate = \\d{3,15}, runtime=1\\d{3} where id = "+experiment.id;
		dbUtils.executeNativeQueryOrRollBack(matches(query));
		query = "UPDATE COMPARISONEXPERIMENT set status='Running' where id = "+experiment.id;
		//TODO This should be executed but right now we have a bug that blocks the tx with this query
//		dbUtils.executeNativeQueryOrRollBack(query);
		
		replay(factory, engine, dbUtils);

		SimilarityCalculation sc1 = new SimilarityCalculation(alignment, factory);
		sc1.doJob();
		
		experiment.refresh();
		
		assertTrue(alignment.finished);
		assertFalse(alignment.error);
		assertTrue(alignment.runTime >= 1000);
		verify(factory, engine, dbUtils);
	}

	@Test
	public void intermediateSimilarityCalculationLaunch(){
		ComparisonExperiment experiment = creator.getSmallEvaluatedExperiment(owner);
		experiment.alignments.get(1).finished = false;
		Alignment alignment = experiment.alignments.get(0);
		alignment.runTime = 1000;
		alignment.finished = false;
		experiment.save();
		
		Engine engine = createMock(Engine.class);
		engine.calculate(alignment, SlurmExternalScript.DEFAULT_PARTITION);
		expect(factory.getEngine(experiment)).andReturn(engine);
		expect(factory.getPriorityJobQueue()).andReturn(priorityJobQueue);

		replay(factory, engine);

		SimilarityCalculation sc1 = new SimilarityCalculation(alignment, factory);
		
		sc1.doJob();
		
		assertTrue(alignment.finished);
		assertFalse(alignment.error);
		assertTrue(alignment.runTime >= 1000);
		assertEquals(0, experiment.runTime);
		assertEquals(0, experiment.endDate);
		verify(factory, engine);
	}

	@Test
	public void errorInSimilarityCalculation() {
		ComparisonExperiment experiment = creator.getSmallEvaluatedExperiment(owner);
		experiment.alignments.get(1).finished = false;
		Alignment alignment = experiment.alignments.get(0);
		alignment.runTime = 1000;
		alignment.finished = false;
		experiment.save();
		
		expect(factory.getPriorityJobQueue()).andReturn(priorityJobQueue);
		Engine engine = createMock(Engine.class);
		engine.calculate(alignment, SlurmExternalScript.DEFAULT_PARTITION);
		expectLastCall().andThrow(new RuntimeException());

		expect(factory.getEngine(experiment)).andReturn(engine);

		replay(factory, engine);
		SimilarityCalculation sc1 = new SimilarityCalculation(alignment, factory);
		sc1.doJob();
		
		assertTrue(alignment.finished);
		assertTrue(alignment.error);
		assertTrue(alignment.runTime >= 1000);
		assertEquals(0, experiment.runTime);
		assertEquals(0, experiment.endDate);
		verify(factory, engine);
	}

	@Test
	public void knossosJobTellsQueueWhenFinished() {
		ComparisonExperiment experiment = creator.getSmallEvaluatedExperiment(owner);
		Alignment alignment = experiment.alignments.get(0);

		priorityJobQueue.finishedJob(anyObject(SimilarityCalculation.class));

		replay(priorityJobQueue, factory);

		SimilarityCalculation job = new SimilarityCalculation(alignment, factory);
		
		job.removeFromQueue();
		
		verify(priorityJobQueue, factory);
	}
	
	@Test
	public void setsErrorToFalseAfterCalculationFinishesCorrectly(){
		ComparisonExperiment experiment = creator.getSmallEvaluatedExperiment(owner);
		Alignment alignment = experiment.alignments.get(0);
		alignment.runTime = 1000;
		alignment.finished = false;
		alignment.error = true;
		experiment.save();
		
		Engine engine = createMock(Engine.class);
		engine.calculate(alignment, SlurmExternalScript.DEFAULT_PARTITION);
		expect(factory.getEngine(experiment)).andReturn(engine);
		expect(factory.getPriorityJobQueue()).andReturn(priorityJobQueue);
		expect(factory.getExternalScriptLauncher()).andReturn(new SlurmExternalScript()).anyTimes();

		replay(factory, engine);

		SimilarityCalculation sc1 = new SimilarityCalculation(alignment, factory);
		
		sc1.doJob();
		
		assertTrue(alignment.finished);
		assertFalse(alignment.error);
		verify(factory, engine);
	}
	
	@Test
	public void setsExperimentToRunning(){
		ComparisonExperiment experiment = creator.getSmallEvaluatedExperiment(owner);
		Alignment alignmentToBeCalculated = experiment.alignments.get(0);
		Alignment pendingAlignment = experiment.alignments.get(1);
		alignmentToBeCalculated.finished = false;
		pendingAlignment.finished = false;
		experiment.save();
		
		Engine engine = createMock(Engine.class);
		engine.calculate(alignmentToBeCalculated, SlurmExternalScript.DEFAULT_PARTITION);
		expect(factory.getPriorityJobQueue()).andReturn(priorityJobQueue);
		expect(factory.getPriorityJobQueue()).andReturn(priorityJobQueue);
		expect(factory.getEngine(experiment)).andReturn(engine);
                                

//		String query = "UPDATE COMPARISONEXPERIMENT set status='Running' where id = "+experiment.id;
		//TODO This should be executed but right now we have a bug that blocks the tx with this query
//		dbUtils.executeNativeQueryOrRollBack(query);
		
		replay(factory, engine, dbUtils);

		SimilarityCalculation sc1 = new SimilarityCalculation(alignmentToBeCalculated, factory);
		
		sc1.doJob();
		
		verify(dbUtils);
	}

	@Test
	public void setsExperimentToFinishedWhenLastAlignmentIsCalculated(){
		ComparisonExperiment experiment = creator.getSmallEvaluatedExperiment(owner);
		Alignment lastAlignmentToBeCalculated = experiment.alignments.get(0);
		lastAlignmentToBeCalculated.finished = false;
		experiment.save();
		
		Engine engine = createMock(Engine.class);
		engine.calculate(lastAlignmentToBeCalculated, SlurmExternalScript.DEFAULT_PARTITION);
		expect(factory.getPriorityJobQueue()).andReturn(priorityJobQueue);
		expect(factory.getExternalScriptLauncher()).andReturn(new SlurmExternalScript()).anyTimes();
		expect(factory.getEngine(experiment)).andReturn(engine);

		String query = "UPDATE COMPARISONEXPERIMENT set status='Running' where id = "+experiment.id;
		//TODO This should be executed but right now we have a bug that blocks the tx with this query
//		dbUtils.executeNativeQueryOrRollBack(query);
		query = "UPDATE COMPARISONEXPERIMENT set status='Finished', endDate = \\d{3,15}, runtime=\\d where id = "+experiment.id;
		dbUtils.executeNativeQueryOrRollBack(matches(query));
		
		replay(factory, engine, dbUtils);

		SimilarityCalculation sc1 = new SimilarityCalculation(lastAlignmentToBeCalculated, factory);
		
		sc1.doJob();
		
		verify(dbUtils);
	}

	
}
