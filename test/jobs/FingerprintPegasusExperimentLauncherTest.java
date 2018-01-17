package jobs;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;

import jobs.comparison.FingerprintPegasusExperimentLauncher;

import models.ComparisonExperiment;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;
import utils.TemplatedConfiguration;
import utils.experiment.TestDataCreator;
import utils.scripts.ExternalScript;
import files.DatabaseFiles;
import files.FileUtils;

public class FingerprintPegasusExperimentLauncherTest extends UnitTest {

	TestDataCreator creator;

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		creator = new TestDataCreator();
	}

	@Test
	public void fingerprintPegasusExperimentLauncherBehavesCorrectly(){
		String username = "dbermudez";
		User owner = User.findByUserName(username);
		Factory factory = createMock(Factory.class);
		ComparisonExperiment experiment = creator.getSmallExperiment(owner);
        experiment.engineId = Factory.FINGERPRINTS_GRAPH;
        experiment.engineName = Factory.FINGERPRINTS_GRAPH_NAME;
		experiment.save();
		DatabaseFiles databaseFiles = createMock(DatabaseFiles.class);
		expect(databaseFiles.getFileName(experiment.targetMolecules)).andReturn("targetFile");
		expect(databaseFiles.getFileName(experiment.probeMolecules)).andReturn("probeFile");
		FileUtils fileUtils = createMock(FileUtils.class);
		String outputPath = TemplatedConfiguration.get("tmp.dir") + "/" + experiment.id;
		fileUtils.createDirectory(outputPath);
		String targetInputFile = outputPath+"/targetDatabase.smi";
		String probeInputFile = outputPath+"/probeDatabase.smi";
		fileUtils.copyFile("probeFile", probeInputFile);
		fileUtils.copyFile("targetFile", targetInputFile);
		ExternalScript externalScriptLauncher = createMock(ExternalScript.class);
		String commandTemplate = TemplatedConfiguration.get("fingerprintSimilarity");
		String outputFile = TemplatedConfiguration.get("tmp.dir") + "/" + experiment.id + "/Similarities_" + 
		experiment.engineName + "_" + experiment.id + ".csv";		
		String command = String.format(commandTemplate, probeInputFile, targetInputFile, "graph", outputFile);
		expect(externalScriptLauncher.paralelize(1, "FINGERPRINT", command)).andReturn("");
		databaseFiles.store(experiment, new File(outputFile));
		
		FingerprintPegasusExperimentLauncher launcher = new FingerprintPegasusExperimentLauncher(experiment.id, owner, factory);
		launcher.setFileUtils(fileUtils);
		launcher.setLauncher(externalScriptLauncher);
		launcher.setDatabaseFiles(databaseFiles);
		
		replay(factory, fileUtils, externalScriptLauncher, databaseFiles);
		
		launcher.doJob();
		
		verify(factory, fileUtils, externalScriptLauncher, databaseFiles);
	}
	
}