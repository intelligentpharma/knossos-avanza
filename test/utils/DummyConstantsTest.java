package utils;

import models.EventLevel;
import models.ExperimentStatus;
import models.MoleculeDatabaseStatus;

import org.junit.Test;

import play.test.UnitTest;

public class DummyConstantsTest extends UnitTest{

	@Test
	public void MoleculeDatabaseStatusConstantsOk(){
		MoleculeDatabaseStatus databaseStatus = new MoleculeDatabaseStatus();
		assertEquals(databaseStatus.AVAILABLE, "Available");
		assertEquals(databaseStatus.NOT_AVAILABLE, "Not available");
	}
	
	@Test
	public void EventLevelConstantsOk(){
		EventLevel eventLevel = new EventLevel();
		assertEquals(eventLevel.ERROR, "Error");
		assertEquals(eventLevel.INFO, "Info");
		assertEquals(eventLevel.WARNING, "Warning");
	}

	@Test
	public void ExperimentStatusConstantsOk(){
		ExperimentStatus experimentStatus = new ExperimentStatus();
		assertEquals(experimentStatus.ERROR, "Error");
		assertEquals(experimentStatus.FINISHED, "Finished");
		assertEquals(experimentStatus.QUEUED, "Queued");
		assertEquals(experimentStatus.RUNNING, "Running");
	}
}
