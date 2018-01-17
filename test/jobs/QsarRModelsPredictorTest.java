package jobs;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import jobs.qsar.QsarRModelsPredictor;
import jobs.qsar.RScriptLauncher;
import models.MoleculeDatabase;
import models.QsarExperiment;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.TemplatedConfiguration;
import utils.experiment.TestDataCreator;
import utils.scripts.ExternalScript;
import de.zbit.jcmapper.tools.moltyping.enumerations.EnumerationsAtomTypes.AtomLabelType;
import files.DatabaseFiles;
import files.FileUtils;

public class QsarRModelsPredictorTest extends UnitTest{
	
	String fileDirectory;
	QsarRModelsPredictor launcher;
	QsarExperiment experiment;
	RScriptLauncher rscriptLauncher;
	DatabaseFiles databaseFiles;
	FileUtils utils;
	MoleculeDatabase database;
	TestDataCreator creator;
	User user;

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		user = User.findByUserName("dbermudez");
		creator = new TestDataCreator();
		database = creator.createSmallDatabaseWithoutProperties(user);
		rscriptLauncher = createNiceMock(RScriptLauncher.class);
		databaseFiles = createNiceMock(DatabaseFiles.class);
		utils = createNiceMock(FileUtils.class);
		experiment = creator.createQsarExperiment(user);
		launcher = new QsarRModelsPredictor(database.id, experiment.id);
		launcher.setLauncher(rscriptLauncher);
		launcher.setDatabaseFiles(databaseFiles);
		launcher.setFileUtils(utils);
	}
	
	@Test
	public void launcherPredictionRscript(){
		String commandTemplate = TemplatedConfiguration.get("prediction.qsar");
		expect(databaseFiles.getExperimentBasePath()).andReturn("directory");
		expect(databaseFiles.getFileName(database)).andReturn("test-files/pepito.sdf");
		utils.createDirectory(anyObject(String.class));
		utils.copyDirectory(anyObject(String.class), anyObject(String.class));
		utils.copyFile(anyObject(String.class), anyObject(String.class));
		expect(rscriptLauncher.launchRScript(commandTemplate)).andReturn("nothing");
		replay(rscriptLauncher, databaseFiles, utils);
		launcher.launch();
		verify(rscriptLauncher, databaseFiles, utils);
	}

}
