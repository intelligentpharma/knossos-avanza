package files;

import java.util.List;

import models.Deployment;
import models.MoleculeDatabase;
import models.User;

import org.junit.Before;
import org.junit.Test;

import files.formats.csv.DatabaseActionOutputCsvParser;
import files.formats.csv.MoleculeDatabaseClusterizationParser;

import play.test.UnitTest;
import utils.TemplatedConfiguration;
import utils.experiment.TestDataCreator;

public class MoleculeDatabaseClusterizationParserTest extends UnitTest{
	DatabaseActionOutputCsvParser parser = new MoleculeDatabaseClusterizationParser();
	MoleculeDatabase database;
	TestDataCreator dataCreator = new TestDataCreator();
	User user;
	FileUtils utils = new FileUtilsImpl();
	TemplatedConfiguration configuration = new TemplatedConfiguration();

	@Before
	public void setup() {
		user = new User("aperreau","hola","adeu");
		user.save();
		database = dataCreator.createSmallDatabaseWithoutProperties(user);
		parser.setDatabase(database);
		utils.createDirectory(configuration.get("tmp.dir") + database.id);
		utils.copyFile("test-files/qsar/clusterization.csv",
				configuration.get("tmp.dir") + "/" + database.id + "/clusterization.csv");
		dataCreator.createSequenceForTestDb();
	}

	@Test
	public void parseRcdkDescriptorsCsvCorrectly(){
		parser.parseFileAndUpdate();	
		
		String [] cluster = {"1","2","3","4","4","3"};
		
		for(int i=0; i<database.molecules.size(); i++){
			List<Deployment> deploymentList = 
					Deployment.findDeploymentsByDatabaseAndMoleculeName(database, database.molecules.get(i).name);
			for(Deployment deployment : deploymentList){
				assertEquals(cluster[i],deployment.getPropertyValueThroughSql("Cluster"));
			}
		}		
	}


	

}
