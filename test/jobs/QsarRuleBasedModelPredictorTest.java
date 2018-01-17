package jobs;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jobs.qsar.QsarRuleBasedModelPredictor;
import junitx.framework.FileAssert;
import models.MoleculeDatabase;
import models.QsarExperiment;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;
import utils.TemplatedConfiguration;
import utils.experiment.TestDataCreator;
import files.DatabaseFiles;
import files.FileUtils;

public class QsarRuleBasedModelPredictorTest extends UnitTest{
	String fileDirectory;
	QsarRuleBasedModelPredictor predictor;
	DatabaseFiles databaseFiles;
	FileUtils utils;
	MoleculeDatabase database;
	TestDataCreator creator;
	User user;
	QsarExperiment experiment;
	Factory factory;

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		user = User.findByUserName("dbermudez");
		factory = createNiceMock(Factory.class);
		creator = new TestDataCreator();
        List<String> propertyNames = new ArrayList<String>();
        propertyNames.add("prop1");
        propertyNames.add("prop2");
        propertyNames.add("prop3");
        propertyNames.add("prop4");
        database = creator.createSmallDatabaseWithPropertyNamesAndValues(user, propertyNames);
		databaseFiles = createNiceMock(DatabaseFiles.class);
		utils = createNiceMock(FileUtils.class);
		experiment = creator.createQsarExperimentWithRulesAndDeploymentsWithProperties(user);
		predictor = new QsarRuleBasedModelPredictor(database.id, experiment.id);
		predictor.setDatabaseFiles(databaseFiles);
		predictor.setFileUtils(utils);
	}
	
	@Test
	public void predictionRuleBasedModel(){
		expect(databaseFiles.getPath(experiment)).andReturn("test-files/qsar/ruleBased");
		utils.createDirectory(anyObject(String.class));
		utils.copyDirectory(anyObject(String.class), anyObject(String.class));
		replay(databaseFiles, utils);
		
		String outputPath = TemplatedConfiguration.get("tmp.dir") + "/" + database.id + "/prediction_"+experiment.id+".csv";
		predictor.launch();
		
		FileAssert.assertEquals(new File("test-files/qsar/ruleBased/prediction.csv"), new File(outputPath));
		
		verify(databaseFiles, utils);
		
	}

}
