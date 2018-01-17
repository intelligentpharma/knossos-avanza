package jobs;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import jobs.qsar.QsarExperimentRemovalJob;
import models.QsarExperiment;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.DatabaseAccess;
import utils.experiment.TestDataCreator;

public class QsarExperimentRemovalJobTest extends UnitTest {

	private TestDataCreator dataCreator;
	private User owner;
	
	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		dataCreator = new TestDataCreator();
		owner = User.findByUserName("test");
	}
	
	@Test
	public void qsarExperimentIsErasedCorrectly(){
		QsarExperiment experiment = dataCreator.createQsarExperiment(owner);
		QsarExperimentRemovalJob job = new QsarExperimentRemovalJob();
		
		DatabaseAccess dbAccess = createMock(DatabaseAccess.class);
		dbAccess.delete(anyObject(QsarExperiment.class));
		job.setDatabaseAccess(dbAccess);
		
		job.setExperimentId(experiment.id);
		
		replay(dbAccess);

		job.doJob();
		
		verify(dbAccess);
	}

	@Test
	public void nothingHappensTryingToDeleteNonExistantQsarExperiment(){
		QsarExperimentRemovalJob job = new QsarExperimentRemovalJob();
		
		DatabaseAccess dbAccess = createMock(DatabaseAccess.class);
		//dbAccess.delete(anyObject(QsarExperiment.class)); //Commented out because nothing happens!
		job.setDatabaseAccess(dbAccess);
		
		job.setExperimentId(666L);
		
		replay(dbAccess);

		job.doJob();
		
		verify(dbAccess);
	}
	
}
