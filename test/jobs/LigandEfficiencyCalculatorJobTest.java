package jobs;

import static org.easymock.EasyMock.*;

import java.io.File;

import jobs.database.calculate.LigandEfficiencyCalculatorJob;
import jobs.database.calculate.PropertyCalculatorJob;

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

public class LigandEfficiencyCalculatorJobTest extends UnitTest {

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

		PropertyCalculatorJob calculatorJob = new LigandEfficiencyCalculatorJob(database.id, ChemicalProperty.DEPLOYMENT_ORDER);
		calculatorJob.setDatabaseFiles(dbFiles);
		calculatorJob.setPropertyExtractor(extractor);
		File file = new File("test-files/test2.sdf");
		expect(dbFiles.retrieve(database)).andReturn(file);
		extractor.parse(file);
		expect(extractor.getNhea(anyLong())).andReturn(3).anyTimes();
		
		replay(dbFiles, extractor);
		
		calculatorJob.doJob();
		database.refresh();
	}
	
	@Test
	public void calculatedLigandEfficiencyCorrectly(){
		for(Deployment deployment : database.getAllDeployments()){
			double ligandEfficiency = 1.4 * Double.parseDouble(deployment.getPropertyValue(ChemicalProperty.DEPLOYMENT_ORDER)) / 3;
			assertEquals(ligandEfficiency, Double.parseDouble(deployment.getPropertyValue(ChemicalProperty.LE)), 0.001);
		}
		
		verify(dbFiles, extractor);
	}
}
