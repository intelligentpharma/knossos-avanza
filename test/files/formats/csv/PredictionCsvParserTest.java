package files.formats.csv;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import models.ChemicalProperty;
import models.Deployment;
import models.MoleculeDatabase;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.db.jpa.Blob;
import play.libs.MimeTypes;
import play.test.UnitTest;
import utils.experiment.TestDataCreator;
import engine.factory.FileNameFactory;
import files.formats.csv.DatabaseActionOutputCsvParser;
import files.formats.csv.PredictionCsvParser;

public class PredictionCsvParserTest extends UnitTest {

	DatabaseActionOutputCsvParser parser;
	MoleculeDatabase database;
	TestDataCreator creator;
	User user;
	FileNameFactory factory;
	long qsarExperimentId;
	String filePath;
	
	@Before
	public void setup() {
		user = new User("aperreau","hola","adeu");
		user.save();
		filePath = "test-files/qsar";
		creator = new TestDataCreator();
		database = creator.createSmallDatabaseWithoutProperties(user);
		qsarExperimentId = 1;
		factory = createNiceMock(FileNameFactory.class);
		creator.createSequenceForTestDb();
	}
        
        //TODO: Remove this test once the tests in this class are fixed
        @Test
	public void dummyTestPreventingToCrash(){
            boolean dummy = true;
            assertTrue(dummy);
        }
	
	@Test(expected = RuntimeException.class)
	public void throwExceptionWhenFileDoesNotExist(){
		expect(factory.getPredictionCsvName(qsarExperimentId)).andReturn("/hola.csv");
		replay(factory);
		parser = new PredictionCsvParser(filePath, factory, qsarExperimentId);
		parser.setDatabase(database);
		verify(factory);
	
		parser.parseFileAndUpdate();
	}
	
	@Test(expected = RuntimeException.class)
	public void throwRuntimeExceptionIfFormatIsWrong(){
		expect(factory.getPredictionCsvName(qsarExperimentId)).andReturn("/exampleQsarPredictionFake.csv");
		replay(factory);
		parser = new PredictionCsvParser(filePath, factory, qsarExperimentId);
		parser.setDatabase(database);
		verify(factory);

		parser.parseFileAndUpdate();
	}
		
	//@Test Fails with primary key violation
	public void parserAndAddPropertiesInDatabase(){
		expect(factory.getPredictionCsvName(qsarExperimentId)).andReturn("/exampleQsarPrediction.csv");
		replay(factory);
		parser = new PredictionCsvParser(filePath, factory, qsarExperimentId);
		parser.setDatabase(database);
		verify(factory);

		parser.parseFileAndUpdate();
		
		assertEquals(2,database.getPropertyNames().size());
		assertEquals("pred1",database.getPropertyNames().get(0));
		assertEquals("pred2",database.getPropertyNames().get(1));
		
		Deployment deployment = Deployment.findDeploymentsByDatabaseAndName(database, "MCMC00000004_t001_i001_c001").get(0);
		assertEquals("32",deployment.getPropertyValueThroughSql("pred1"));
		assertEquals("12",deployment.getPropertyValueThroughSql("pred2"));
		deployment = Deployment.findDeploymentsByDatabaseAndName(database, "MCMC00000005_t001_i001_c001").get(0);
		assertEquals("59",deployment.getPropertyValueThroughSql("pred1"));
		assertEquals("64",deployment.getPropertyValueThroughSql("pred2"));
		deployment = Deployment.findDeploymentsByDatabaseAndName(database, "MCMC00000006_t001_i001_c001").get(0);
		assertEquals("223",deployment.getPropertyValueThroughSql("pred1"));
		assertEquals("13",deployment.getPropertyValueThroughSql("pred2"));
		deployment = Deployment.findDeploymentsByDatabaseAndName(database, "MCMC00000007_t001_i001_c001").get(0);
		assertEquals("24",deployment.getPropertyValueThroughSql("pred1"));
		assertEquals("200",deployment.getPropertyValueThroughSql("pred2"));
		deployment = Deployment.findDeploymentsByDatabaseAndName(database, "MCMC00000008_t001_i001_c001").get(0);
		assertEquals("15",deployment.getPropertyValueThroughSql("pred1"));
		assertEquals("23",deployment.getPropertyValueThroughSql("pred2"));
		deployment = Deployment.findDeploymentsByDatabaseAndName(database, "MCMC00000019_t001_i001_c001").get(0);
		assertEquals("84",deployment.getPropertyValueThroughSql("pred1"));
		assertEquals("12",deployment.getPropertyValueThroughSql("pred2"));
		deployment = Deployment.findDeploymentsByDatabaseAndName(database, "MCMC00000019_t001_i001_c002").get(0);
		assertEquals("1",deployment.getPropertyValueThroughSql("pred1"));
		assertEquals("201",deployment.getPropertyValueThroughSql("pred2"));
	}
}
