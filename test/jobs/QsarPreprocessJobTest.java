package jobs;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;

import jobs.qsar.QsarPreprocessJob;

import models.MoleculeDatabase;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.DBUtils;
import utils.experiment.TestDataCreator;
import utils.scripts.ExternalScript;
import visitors.CsvGenerator;
import files.DatabaseFiles;
import files.formats.csv.DatabaseActionOutputCsvParser;
import files.formats.csv.MoleculeDatabaseCsvParser;

public class QsarPreprocessJobTest extends UnitTest{
	
	QsarPreprocessJob job;
	
	DatabaseFiles dbFiles;
	DBUtils dbUtils;
	ExternalScript launcher;
	CsvGenerator csvGenerator;
	DatabaseActionOutputCsvParser parser;
	
	MoleculeDatabase database;

	@Before
	public void setup(){
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");

		TestDataCreator creator = new TestDataCreator();
		User owner = User.findByUserName("test");
		database = creator.createSingleMoleculeDatabase(owner);
		job = new QsarPreprocessJob(database.id);
		
		dbFiles = createMock(DatabaseFiles.class);
		dbUtils = createMock(DBUtils.class);
		launcher = createMock(ExternalScript.class);
		csvGenerator = createMock(CsvGenerator.class);
		parser = createMock(MoleculeDatabaseCsvParser.class);
                
                String [] descriptors = {"hola", "adeu"};
		
		job.setDatabaseFiles(dbFiles);
		job.setDBUtils(dbUtils);
		job.setLauncher(launcher);
		job.setMoleculeDatabaseCsvGenerator(csvGenerator);
		job.setMoleculeDatabaseCsvParser(parser);
        job.setPreprocessConfigurationParams(descriptors, descriptors, 10, 20, 0.1);
	}
	
	@Test
	public void visitsDatabaseAndExtractsCsv(){
		csvGenerator.visit(database);
		expect(csvGenerator.getCsv()).andReturn("some,csv,text");
		replay(csvGenerator);
		job.doJob();
		verify(csvGenerator);
	}

	@Test
	public void executesExternalRScript(){
		String command = String.format("/usr/local/bin/R --vanilla --slave -f ./scripts/databasePreProcess.R " +
				"--args ./tmp/qsarPreprocessInput%s.csv ./tmp/qsarPreprocessOutput%s.csv ./tmp/notToRemoveDescriptors_%s.csv ./tmp/notToCorrelateDescriptors_%s.csv %s %s %s", database.id, database.id, database.id, database.id, "10", "20", "0.1");
		expect(launcher.paralelize(1, "QSARpreprocess", command)).andReturn("OK");
		
		replay(launcher);
		job.doJob();
		verify(launcher);		
	}
	
	@Test
	public void parsesROutputFile(){
		parser.parseFileAndUpdate();
		expect(parser.getData()).andReturn(database.getAllDeployments()).anyTimes();
		
		replay(parser);
		job.doJob();
		verify(parser);		
	}
	
	//TODO descomentar este test! No funciona por el QsarPreprocessJob...
	//@Test
	public void storesPreprocessLog(){
		parser.parseFileAndUpdate();
		expect(parser.getData()).andReturn(database.getAllDeployments()).anyTimes();
		dbFiles.storePreprocessLog(anyObject(MoleculeDatabase.class), anyObject(File.class));

		replay(parser, dbFiles);
		job.doJob();
		verify(dbFiles);
	}
}
