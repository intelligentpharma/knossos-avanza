package jobs;

import static org.easymock.EasyMock.*;
import jobs.database.calculate.SmileDatabaseCounterIonsRemovalJob;

import models.MoleculeDatabase;
import models.User;

import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;
import utils.database.CounterIonsRemover;
import utils.experiment.TestDataCreator;
import files.DatabaseFiles;

public class SmileDatabaseCounterIonsRemovalJobTest extends UnitTest{

	@Test
	public void SmileDatabaseCounterIonsRemovedCorrectly(){
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		
		TestDataCreator creator = new TestDataCreator();
		User owner = User.findByUserName("test");
		MoleculeDatabase database = creator.createSingleMoleculeDatabase(owner);
		long moleculeDatabaseId = database.getId();
		String smilesFileName = "molDB.file";

		Factory factory = createMock(Factory.class);
		CounterIonsRemover counterIonsRemover = createMock(CounterIonsRemover.class);
		counterIonsRemover.removeCounterIons(smilesFileName);
		DatabaseFiles databaseFiles = createMock(DatabaseFiles.class);
		expect(databaseFiles.getFileName(anyObject(MoleculeDatabase.class))).andReturn(smilesFileName);
		expect(factory.getCounterIonsRemover()).andReturn(counterIonsRemover);
		expect(factory.getDatabaseFiles()).andReturn(databaseFiles);
		
		SmileDatabaseCounterIonsRemovalJob counterIonsRemovalJob = new SmileDatabaseCounterIonsRemovalJob(moleculeDatabaseId, factory);

		replay(factory, counterIonsRemover, databaseFiles);
		
		counterIonsRemovalJob.doJob();
		
		verify(factory, counterIonsRemover, databaseFiles);
	}
	
	
}
