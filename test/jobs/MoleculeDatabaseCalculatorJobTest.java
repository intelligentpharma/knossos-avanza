package jobs;

import static org.easymock.EasyMock.*;

import java.io.File;

import jobs.database.calculate.MoleculeDatabaseCalculatorJob;

import models.ChemicalProperty;
import models.Deployment;
import models.MoleculeDatabase;
import models.User;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.experiment.TestDataCreator;
import files.DatabaseFiles;
import files.DeploymentPropertyExtractor;

public class MoleculeDatabaseCalculatorJobTest extends UnitTest {

	private static User user;
	private MoleculeDatabase database;
	private DatabaseFiles dbFiles;
	private DeploymentPropertyExtractor extractor;

	@BeforeClass
	public static void setupClass() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		user = User.findByUserName("aperreau");
	}
	
	@Before
	public void setup(){
		TestDataCreator testFactory = new TestDataCreator();
		database = testFactory.createTwoMoleculesFourConformationsDatabase(user);
		database.save();
		dbFiles = createMock(DatabaseFiles.class);
		extractor = createMock(DeploymentPropertyExtractor.class);

		MoleculeDatabaseCalculatorJob calculatorJob = new MoleculeDatabaseCalculatorJob(database.id);
		calculatorJob.setDatabaseFiles(dbFiles);
		calculatorJob.setPropertyExtractor(extractor);
		File file = new File("test-files/test2.sdf");
		expect(dbFiles.retrieve(database)).andReturn(file);
		extractor.parse(file);
		expect(extractor.getNpol(anyLong())).andReturn(1).anyTimes();
		expect(extractor.getMolecularWeight(anyLong())).andReturn("2").anyTimes();
		expect(extractor.getNhea(anyLong())).andReturn(3).anyTimes();
		
		replay(dbFiles, extractor);
		
		calculatorJob.doJob();
		database.refresh();
	
	}
	

	@Test
	public void setsCalculatedPropertiesFlagToTrue(){
		assertTrue(database.calculatedProperties);
		
		verify(dbFiles, extractor);
	}

	@Test
	public void calculatesPropertiesCorrectly(){
		
		for(Deployment deployment: database.getAllDeployments()){
			assertNotNull(deployment.getPropertyValue(ChemicalProperty.NPOL));
			assertNotNull(deployment.getPropertyValue(ChemicalProperty.NHEA));
			assertNotNull(deployment.getPropertyValue(ChemicalProperty.MW));
		}
	}
	
}