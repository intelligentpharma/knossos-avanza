package jobs;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;

import jobs.database.calculate.RcdkDescriptorsCalculatorJob;
import jobs.qsar.RScriptLauncher;

import models.MoleculeDatabase;
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
import files.formats.csv.DatabaseActionOutputCsvParser;

public class RcdkDescriptorsCalculatorJobTest extends UnitTest{
	TestDataCreator creator;
	MoleculeDatabase database;
	User user;
	Factory factory;
	RScriptLauncher rscriptLauncher;
	DatabaseFiles databaseFiles;
	DatabaseActionOutputCsvParser parser;
	RcdkDescriptorsCalculatorJob launcher;
	FileUtils fileUtils;
	String descriptorsType;

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		user = User.findByUserName("aperreau");
		creator = new TestDataCreator();
		database = creator.createSmallDatabaseWithoutProperties(user);
		factory = createNiceMock(Factory.class);
		rscriptLauncher = createNiceMock(RScriptLauncher.class);
		databaseFiles = createNiceMock(DatabaseFiles.class);
		parser = createNiceMock(DatabaseActionOutputCsvParser.class);
		fileUtils = createMock(FileUtils.class);

		descriptorsType = "2D + 3D";
		launcher = new RcdkDescriptorsCalculatorJob(database.id, descriptorsType);
		launcher.setDatabaseFiles(databaseFiles);
		launcher.setLauncher(rscriptLauncher);
		launcher.setParser(parser);
		launcher.setFileUtils(fileUtils);
	}
	
	@Test
	public void launcherParallelizeRscript(){
		String databaseFile = "database file"; 
		expect(databaseFiles.getFileName(database)).andReturn(databaseFile);
		String outputPath = TemplatedConfiguration.get("tmp.dir")+"/"+database.id;
		String inputPath = outputPath + ".input";
		List<String> parameters = new ArrayList<String>();
		parameters.add(inputPath);
		parameters.add(descriptorsType);
		parameters.add(outputPath);
		rscriptLauncher.addParameters(parameters);
		expect(rscriptLauncher.launchRScript(TemplatedConfiguration.get("descriptors.calculator"))).andReturn("nothing");
		
		replay(factory, rscriptLauncher, databaseFiles, parser);
		launcher.doJob();
		verify(rscriptLauncher);
	}
	
	@Test
	public void callsParseFileOnParser(){
		parser.parseFileAndUpdate();
		replay(factory, rscriptLauncher, databaseFiles, parser);
		launcher.doJob();
		verify(parser);
	}

	@Test
	public void copiesInputFileToGrid(){
		String databaseFile = "database file"; 
		expect(databaseFiles.getFileName(database)).andReturn(databaseFile);
		String directory = TemplatedConfiguration.get("tmp.dir")+"/"+database.id;
		String copiedFile = directory + ".input";
		fileUtils.createDirectory(directory);
		fileUtils.copyFile(databaseFile, copiedFile);
		expectLastCall().once();
		
		replay(fileUtils, databaseFiles);
		launcher.doJob();
		verify(fileUtils);
	}
}
