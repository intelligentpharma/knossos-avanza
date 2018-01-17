package jobs;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;

import jobs.database.analyze.BoxPlotJob;

import models.MoleculeDatabase;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.database.MoleculeDatabaseBoxPlotter;
import utils.experiment.TestDataCreator;
import files.DatabaseFiles;

public class BoxPlotJobTest extends UnitTest{
	
	MoleculeDatabase database;
	DatabaseFiles databaseFiles;
	MoleculeDatabaseBoxPlotter databaseBoxPlotter;
	BoxPlotJob boxplotJob;
	
	@Before
	public void setup(){
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
		User user = User.findByUserName("xmaresma");
		TestDataCreator creator = new TestDataCreator();
		database = creator.createSingleMoleculeDatabase(user);
		databaseBoxPlotter = createNiceMock(MoleculeDatabaseBoxPlotter.class);
		databaseFiles = createNiceMock(DatabaseFiles.class);
		boxplotJob = new BoxPlotJob(database.id, databaseBoxPlotter, databaseFiles);
	}
	
	@Test
	public void launchJob(){
		expect(databaseBoxPlotter.generateBoxPlotPDF()).andReturn("nothing");
		databaseFiles.storeBoxplotFile(database, new File("nothing"));
		expect(databaseFiles.retrieveBoxplotFile(database)).andReturn(new File("./test-files/file.dpf"));
		replay(databaseBoxPlotter, databaseFiles);
		boxplotJob.now();
	}

}
