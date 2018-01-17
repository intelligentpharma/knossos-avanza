package models;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import jobs.FullExperimentLauncherTest;
import jobs.comparison.SimilarityCalculation;

import org.easymock.EasyMock;
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

public class TimeInfoTest extends UnitTest {

	String username;
	TestDataCreator creator;
	
	@Before
	public void setUp() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		username = "aperreau";
		creator = new TestDataCreator();
	}

	@Test
	public void createdExperimentHasCreationDate(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		assertNotSame(experiment.creationDate, 0);
	}

	@Test
	public void launchedExperimentHasStartingDate(){
		String username = "aperreau";
		User owner = User.findByUserName(username);
		ComparisonExperiment experiment = creator.getSmallEvaluatedExperiment(owner);
		FullExperimentLauncherTest.launchExperimentWithMocks(experiment);
		
		assertNotSame(experiment.startingDate, 0);
	}
	
	@Test
	public void finishedExperimentHasEndDate(){
		String username = "xmaresma";
		User owner = User.findByUserName(username);
		ComparisonExperiment experiment = creator.getSmallEvaluatedExperiment(owner);
		experiment.save();

		Factory factory = EasyMock.createMock(Factory.class);
		Engine engine = EasyMock.createNiceMock(Engine.class);
		
		expect(factory.getEngine(experiment)).andReturn(engine).anyTimes();
		DBUtils dbUtils = createMock(DBUtils.class);
		expect(factory.getDBUtils()).andReturn(dbUtils).anyTimes();
		PriorityJobQueue priorityJobQueue = EasyMock.createNiceMock(PriorityJobQueue.class);
		priorityJobQueue.finishedJob(EasyMock.anyObject(SimilarityCalculation.class));
		expect(factory.getPriorityJobQueue()).andReturn(priorityJobQueue).anyTimes();
		expect(factory.getExternalScriptLauncher()).andReturn(new SlurmExternalScript()).anyTimes();
	
		
		replay(factory, engine, priorityJobQueue);

		int alignmentCount = 0;
		int totalAlignments = experiment.alignments.size();
//	
		for(Alignment alignment : experiment.alignments){
			if(alignmentCount < totalAlignments - 1){
				alignment.finished = true;
			}
			else{
				SimilarityCalculation sc1 = new SimilarityCalculation(experiment.alignments.get(alignmentCount-1), factory);
				sc1.doJob();
				sc1.removeFromQueue();
				//sc1._finally();
			}
			alignmentCount++;
		}
				
		verify(factory, engine, priorityJobQueue);
		
		assertNotSame(experiment.endDate,0);
	}	
	
}