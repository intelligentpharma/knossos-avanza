package jobs;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.List;

import jobs.qsar.AbstractQsarExperimentLauncher;
import jobs.qsar.QsarRuleBasedExperimentLauncher;
import jobs.qsar.RScriptLauncher;

import models.QsarExperiment;
import models.QsarResult;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;
import utils.FactoryImpl;
import utils.TemplatedConfiguration;
import utils.experiment.TestDataCreator;
import utils.scripts.ExternalScript;
import utils.scripts.ExternalScriptViaCommandLine;
import visitors.CsvGenerator;
import visitors.MoleculeDatabaseCsvGenerator;
import files.DatabaseFiles;
import files.FileUtils;
import files.FileUtilsImpl;
import files.formats.csv.QsarExperimentCsvParser;
import files.formats.csv.QsarExperimentCsvParserResultImpl;

public class qsarRuleBasedExperimentLauncherTest extends UnitTest{

	TestDataCreator creator;
	QsarExperiment experiment;
	User user;
	Factory factory;
	RScriptLauncher rscriptLauncher;
	DatabaseFiles databaseFilesParser;
	DatabaseFiles databaseFilesLauncher;
	QsarExperimentCsvParser parser;
	FileUtils fileUtils;
	AbstractQsarExperimentLauncher launcher;
	CsvGenerator databaseCsvGenerator;
	
	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		user = User.findByUserName("aperreau");
		creator = new TestDataCreator();
		experiment = creator.createQsarExperimentWithRulesAndDeploymentsWithProperties(user);
		factory = createNiceMock(Factory.class);
		rscriptLauncher = createNiceMock(RScriptLauncher.class);
		databaseFilesParser = createNiceMock(DatabaseFiles.class);
		databaseFilesLauncher = createNiceMock(DatabaseFiles.class);
		parser = new QsarExperimentCsvParserResultImpl(experiment);
		parser.setDatabaseFiles(databaseFilesParser);
		fileUtils = createNiceMock(FileUtils.class);
		databaseCsvGenerator = new MoleculeDatabaseCsvGenerator();
	}
	
	private void getQsarExperimentLauncher(String commandId){
		launcher = new QsarRuleBasedExperimentLauncher(experiment.id, factory);
		launcher.setDatabaseFiles(databaseFilesLauncher);
		launcher.setFileUtils(fileUtils);
		launcher.setLauncher(rscriptLauncher);
		launcher.setQsarExperimentCsvParser(parser);
		launcher.setMoleculeDatabaseCsvGenerator(databaseCsvGenerator);
		launcher.setCommand(TemplatedConfiguration.get(commandId));	
	}

	@Test
	public void countAccomplishedRules(){
		getQsarExperimentLauncher("qsar.ruleBased");
		String outputPath = TemplatedConfiguration.get("tmp.dir") + "/" + experiment.id ;
		fileUtils.createDirectory(outputPath);
		expect(databaseFilesLauncher.getPath(experiment)).andReturn(outputPath);
		expect(databaseFilesParser.getFileName(experiment)).andReturn(outputPath + "/qsarResult.csv");
		replay(factory, databaseFilesParser, databaseFilesLauncher, rscriptLauncher);
		launcher.doJob();
		
		List<QsarResult> results = QsarResult.find("select res from QsarResult res where res.experiment = ?", experiment).fetch();
		for(QsarResult result : results){
			assertEquals(result.fittedFull, result.fittedTrain);
		}
		assertEquals("50.0", results.get(0).fittedFull);
		assertEquals("50.0", results.get(1).fittedFull);
		assertEquals("25.0", results.get(2).fittedFull);
	}
}
