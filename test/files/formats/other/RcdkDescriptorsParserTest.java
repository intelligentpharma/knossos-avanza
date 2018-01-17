package files.formats.other;

import java.util.List;

import models.Deployment;
import models.MoleculeDatabase;
import models.User;

import org.junit.Before;
import org.junit.Test;

import files.FileUtils;
import files.FileUtilsImpl;
import files.formats.csv.DatabaseActionOutputCsvParser;
import files.formats.other.RcdkDescriptorsParser;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.TemplatedConfiguration;
import utils.experiment.TestDataCreator;

public class RcdkDescriptorsParserTest extends UnitTest{

	DatabaseActionOutputCsvParser parser = new RcdkDescriptorsParser();
	MoleculeDatabase database;
	TestDataCreator dataCreator = new TestDataCreator();
	User user;
	FileUtils utils = new FileUtilsImpl();
	TemplatedConfiguration configuration = new TemplatedConfiguration();

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
        Fixtures.loadModels("initial-data.yml");

		user = new User("aperreau","hola","adeu");
		user.save();
	}
	
	@Test
	public void getDataReturnsNull(){
		assertNull(parser.getData());
	}

	@Test
	public void parseRcdkDescriptorsCsvCorrectly(){
		database = dataCreator.createSmallDatabaseWithoutProperties(user);
		parser.setDatabase(database);
		utils.createDirectory(configuration.get("tmp.dir") + database.id);
		utils.copyFile("test-files/qsar/qsarRcdkDescriptors.csv",
				configuration.get("tmp.dir") + "/" + database.id + "/qsarDescriptors.csv");
		parser.parseFileAndUpdate();	
		
		String [] apol = {"0.2","0.8","6.5","9.6","4.9","2.6","8.1"};
		String [] bpol = {"1.3","0.5","0.8","0.4","5.4","6.7","3.9"};
		
		List<Deployment> deployments = database.getAllDeployments(); 
		
		for(int i=0; i<deployments.size(); i++){
			List<Deployment> deploymentList = 
					Deployment.findDeploymentsByDatabaseAndName(database, deployments.get(i).name);
			for(Deployment deployment : deploymentList){
				assertEquals(apol[i],deployment.getPropertyValueThroughSql("apol"));
				assertEquals(bpol[i],deployment.getPropertyValueThroughSql("bpol"));
			}
		}		
	}
	
	@Test
	public void parseRcdkDescriptorsWithCommasInMoleculeNames(){
		database = dataCreator.createSmallDatabaseWithoutPropertiesAndCommasInMoleculeNames(user);
		parser.setDatabase(database);
		utils.createDirectory(configuration.get("tmp.dir") + database.id);
		utils.copyFile("test-files/qsar/qsarDescriptorsWithCommasInMoleculeName.csv",
				configuration.get("tmp.dir") + "/" + database.id + "/qsarDescriptors.csv");
		parser.parseFileAndUpdate();	
		
		String [] deploymentsNames = {"MCMC00000004_t001_i001_c001","MCMC000,0019_t001_i001_c001","MCMC000,0019_t001_i001_c002"};
		String [] apol = {"0.2","2.6","5.1"};
		String [] bpol = {"1.3","6.7","1.8"};
		
		List<Deployment> deployments = database.getAllDeployments(); 
		
		for(int i=0; i<deployments.size(); i++){
			List<Deployment> deploymentList = 
					Deployment.findDeploymentsByDatabaseAndName(database, deployments.get(i).name);
			for(Deployment deployment : deploymentList){
				assertEquals(deploymentsNames[i],deployment.name);
				assertEquals(apol[i],deployment.getPropertyValueThroughSql("apol"));
				assertEquals(bpol[i],deployment.getPropertyValueThroughSql("bpol"));
			}
		}		
		
	}
}
