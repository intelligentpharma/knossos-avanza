package jobs;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;

import jobs.comparison.MultipleExperimentRemovalJob;

import models.ComparisonExperiment;
import models.ExperimentStatus;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.DatabaseAccess;
import utils.experiment.TestDataCreator;

public class MultipleExperimentRemovalJobTest extends UnitTest {

	TestDataCreator dataCreator;
	
	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		dataCreator = new TestDataCreator();
	}
	
	@Test(expected = RuntimeException.class)
	public void constructorMustReceiveAListOfExperimentIds(){
		new MultipleExperimentRemovalJob((List<ComparisonExperiment>)null);
	}
	
	@Test(expected = RuntimeException.class)
	public void constructorMustReceiveAnExperiment(){
		new MultipleExperimentRemovalJob((ComparisonExperiment)null);
	}
	
	@Test
	public void nothingErasedWhenExperimentIdListIsEmpty(){
		long initialNumberOfExperiments = ComparisonExperiment.count();
		
		List<ComparisonExperiment> experiments = new ArrayList<ComparisonExperiment>();
		MultipleExperimentRemovalJob job = new MultipleExperimentRemovalJob(experiments);
		
		job.doJob();
		
		long finalNumberOfExperiments = ComparisonExperiment.count();
		assertEquals(initialNumberOfExperiments, finalNumberOfExperiments);
	}

	@Test
	public void singleExperimentIsErasedCorrectly(){
		ComparisonExperiment experiment = dataCreator.createExperiment();
		MultipleExperimentRemovalJob job = new MultipleExperimentRemovalJob(experiment);
		DatabaseAccess dbAccess = createMock(DatabaseAccess.class);
		dbAccess.delete(anyObject(ComparisonExperiment.class));
		job.setDatabaseAccess(dbAccess);
		replay(dbAccess);

		job.doJob();
		
		verify(dbAccess);
	}
	
	@Test
	public void runningExperimentIsNotErased(){		
		ComparisonExperiment experiment = dataCreator.createExperiment();
		experiment.status = ExperimentStatus.RUNNING;

		long initialNumberOfExperiments = ComparisonExperiment.count();
		MultipleExperimentRemovalJob job = new MultipleExperimentRemovalJob(experiment);
		
		job.doJob();
		
		long finalNumberOfExperiments = ComparisonExperiment.count();
		assertEquals(initialNumberOfExperiments, finalNumberOfExperiments);
	}

	
//	@Test
	public void nothingHappensWhenTryingToDeleteNonExistentExperiments(){
		ComparisonExperiment experiment = dataCreator.createExperiment();
		experiment.delete();

		long initialNumberOfExperiments = ComparisonExperiment.count();
		MultipleExperimentRemovalJob job = new MultipleExperimentRemovalJob(experiment);
		
		job.doJob();
		
		long finalNumberOfExperiments = ComparisonExperiment.count();
		assertEquals(initialNumberOfExperiments, finalNumberOfExperiments);
	}


	@Test
	public void multipleExperimentsAreErasedCorrectly(){
		ComparisonExperiment experiment1 = dataCreator.createExperiment();
		ComparisonExperiment experiment2 = dataCreator.createExperiment();
		
		List<ComparisonExperiment> experiments = new ArrayList<ComparisonExperiment>();
		experiments.add(experiment1);
		experiments.add(experiment2);
		MultipleExperimentRemovalJob job = new MultipleExperimentRemovalJob(experiments);
		DatabaseAccess dbAccess = createMock(DatabaseAccess.class);
		dbAccess.delete(anyObject(ComparisonExperiment.class));
		expectLastCall().times(2);
		job.setDatabaseAccess(dbAccess);
		replay(dbAccess);
		
		job.doJob();
		
		verify(dbAccess);
	}
	
	@Test
	public void multipleExperimentsWithSomeRunningOnesAreErasedCorrectly(){
		ComparisonExperiment experiment1 = dataCreator.createExperiment();
		ComparisonExperiment experiment2 = dataCreator.createExperiment();
		experiment2.status = ExperimentStatus.RUNNING;
		
		List<ComparisonExperiment> experiments = new ArrayList<ComparisonExperiment>();
		experiments.add(experiment1);
		experiments.add(experiment2);
		MultipleExperimentRemovalJob job = new MultipleExperimentRemovalJob(experiments);
		DatabaseAccess dbAccess = createMock(DatabaseAccess.class);
		dbAccess.delete(anyObject(ComparisonExperiment.class));
		expectLastCall().times(1);
		job.setDatabaseAccess(dbAccess);
		replay(dbAccess);
		
		job.doJob();
		
		verify(dbAccess);
	}
	
	@Test
	public void beingErasedMarkIsUnsetIfDeleteFails(){
		ComparisonExperiment experiment = dataCreator.getSmallEvaluatedExperiment(User.findByUserName("aperreau"));
		
		List<ComparisonExperiment> experiments = new ArrayList<ComparisonExperiment>();
		experiments.add(experiment);

		SecurityManager securityManager = System.getSecurityManager();
		SecurityManager security = createNiceMock(SecurityManager.class);
		security.checkDelete(anyObject(String.class));
		expectLastCall().andThrow(new SecurityException("Cannot delete files in this test"));
		System.setSecurityManager(security);
		
		replay(security);
	 
		MultipleExperimentRemovalJob job = new MultipleExperimentRemovalJob(experiments);
		job.doJob();
		
		assertFalse(experiment.beingErased);
		
		System.setSecurityManager(securityManager);
	}
	
}
