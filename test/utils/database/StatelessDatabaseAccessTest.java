package utils.database;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.IOException;

import models.ComparisonExperiment;
import models.Deployment;
import models.Experiment;
import models.MoleculeDatabase;
import models.QsarExperiment;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.database.StatelessDatabaseAccess;
import utils.experiment.TestDataCreator;
import files.DatabaseFiles;

public class StatelessDatabaseAccessTest extends UnitTest {

	StatelessDatabaseAccess dbAccess;
	DatabaseFiles dbFiles;
	
	@Before
	public void setup(){
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		dbAccess = new StatelessDatabaseAccess();
		dbFiles = createMock(DatabaseFiles.class);
		dbAccess.setDatabaseFiles(dbFiles);
	}
	
	@Test
	public void statelessDeleteRemovesMoleculeDatabaseInfoFromDatabase(){
		User user = User.findByUserName("dbermudez");
		TestDataCreator testFactory = new TestDataCreator();
		MoleculeDatabase database = testFactory.createTwoMoleculesFourConformationsDatabase(user);
		database.save();
		
		long numberOfDatabases = MoleculeDatabase.count();
		
		dbAccess.delete(database);
		
		assertEquals(numberOfDatabases -1, MoleculeDatabase.count());
	}
	
	@Test
	public void statelessDeleteRemovesMoleculeDatabaseAssociatedDirectory(){
		User user = User.findByUserName("dbermudez");
		TestDataCreator testFactory = new TestDataCreator();
		MoleculeDatabase database = testFactory.createTwoMoleculesFourConformationsDatabase(user);
		database.save();
		
		dbFiles.delete(database);
		replay(dbFiles);
		
		dbAccess.delete(database);
		
		verify(dbFiles);
	}
	
	@Test
	public void statelessInsert(){
		User user = User.findByUserName("dbermudez");
		TestDataCreator testFactory = new TestDataCreator();
		MoleculeDatabase database = testFactory.createTwoMoleculesFourConformationsDatabase(user);
		database.save();
		dbAccess.insertMoleculesDeploymentsAndPropertiesInDatabase(database);
		
		assertEquals(4, database.getAllDeployments().size());
		assertEquals(2, database.molecules.size());
	}
	
	@Test
	public void statelessInsertStoresFiles(){
		User user = User.findByUserName("dbermudez");
		TestDataCreator testFactory = new TestDataCreator();
		MoleculeDatabase database = testFactory.createTwoMoleculesFourConformationsDatabase(user);			
		database.originalFileName = "someFile.sdf";
		database.save();
		dbFiles.store(database, database.transientFile);
		dbFiles.store(anyObject(Deployment.class), anyObject(File.class));
		expectLastCall().times(4);
		
		replay(dbFiles);
		
		dbAccess.insertMoleculesDeploymentsAndPropertiesInDatabase(database);
		
		assertEquals(4, database.getAllDeployments().size());
		assertEquals(2, database.molecules.size());
		
		verify(dbFiles);
	}
	
	@Test
	public void statelessDeleteRemovesExperimentInfoFromDatabase(){
		User user = User.findByUserName("dbermudez");
		TestDataCreator testFactory = new TestDataCreator();
		ComparisonExperiment experiment = testFactory.getEvaluatedDockingExperimentWithProperties("test experiment", user);
		
		long numberOfExperiments = ComparisonExperiment.count();
		
		dbAccess.delete(experiment);
		
		assertEquals(numberOfExperiments -1, ComparisonExperiment.count());
	}

	@Test
	public void statelessDeleteRemovesExperimentAssociatedDirectory(){
		User user = User.findByUserName("dbermudez");
		TestDataCreator testFactory = new TestDataCreator();
		ComparisonExperiment experiment = testFactory.getEvaluatedDockingExperimentWithProperties("test experiment", user);
		
		dbFiles.delete((Experiment)experiment);
		replay(dbFiles);
		
		dbAccess.delete(experiment);
		
		verify(dbFiles);
	}

	@Test
	public void statelessDeleteRemovesQsarExperimentInfoFromDatabase(){
		User user = User.findByUserName("dbermudez");
		TestDataCreator dataCreator = new TestDataCreator();
		QsarExperiment experiment = dataCreator.createQsarExperiment(user);
		
		long numberOfExperiments = QsarExperiment.count();
		
		dbAccess.delete(experiment);
		
		assertEquals(numberOfExperiments -1, QsarExperiment.count());
	}

	@Test
	public void statelessDeleteRemovesQsarExperimentAssociatedDirectory(){
		User user = User.findByUserName("dbermudez");
		TestDataCreator dataCreator = new TestDataCreator();
		QsarExperiment experiment = dataCreator.createQsarExperiment(user);
		
		dbFiles.delete((Experiment)experiment);
		replay(dbFiles);
		
		dbAccess.delete(experiment);
		
		verify(dbFiles);
	}

}
