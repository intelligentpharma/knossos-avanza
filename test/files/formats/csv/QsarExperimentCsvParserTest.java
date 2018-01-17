package files.formats.csv;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;

import models.QsarExperiment;
import models.QsarResult;
import models.User;

import org.junit.Before;
import org.junit.Test;

import files.DatabaseFiles;
import files.formats.csv.QsarExperimentCsvParser;
import files.formats.csv.QsarExperimentCsvParserResultImpl;

import play.test.UnitTest;
import utils.experiment.TestDataCreator;

public class QsarExperimentCsvParserTest extends UnitTest {

	QsarExperimentCsvParser parser;
	DatabaseFiles databaseFiles;
	QsarExperiment experiment;
	TestDataCreator dataCreator;
	User user;

	@Before
	public void setup() {
		user = new User("aperreau","hola","adeu");
		user.save();
		dataCreator = new TestDataCreator();
		experiment = dataCreator.createQsarExperiment(user);
		databaseFiles = createMock(DatabaseFiles.class);
		parser = new QsarExperimentCsvParserResultImpl(experiment);
		parser.setDatabaseFiles(databaseFiles);
		dataCreator.createSequenceForTestDb();
	}
	
	@Test
	public void throwExceptionWhenFileDoesNotExist(){
		try{
			executeParser("test-files/qsar/noExampleQsarResult.csv");
			fail("Cannot read Qsar result file");
		}catch(Exception e){
		}
	}
	
	@Test
	public void throwExceptionWhenFileHasIncorrectFormat(){
		try{
			executeParser("test-files/prueba.csv");
			fail("Qsar result file has incorrect format");
		}catch(Exception e){
		}
		
	}

	@Test
	public void throwExceptionWhenFileHasIncorrectNumberFormat2(){
		executeParser("test-files/qsar/exampleQsarResultIncorrectNumberFormat2.csv");
	}

	@Test
	public void throwExceptionWhenFileHasIncorrectNumberFormat3(){
		executeParser("test-files/qsar/exampleQsarResultIncorrectNumberFormat3.csv");
	}

	@Test
	public void throwExceptionWhenFileHasIncorrectNumberFormat4(){
		executeParser("test-files/qsar/exampleQsarResultIncorrectNumberFormat4.csv");
	}

	@Test
	public void throwExceptionWhenFileHasIncorrectNumberFormat5(){
		executeParser("test-files/qsar/exampleQsarResultIncorrectNumberFormat5.csv");
	}
	
	private void executeParser(String fileName) {
			expect(databaseFiles.getFileName(experiment)).andReturn(fileName);
			replay(databaseFiles);
			parser.parseFile();
	}


	@Test
	public void parseFileCorrectly(){
		expect(databaseFiles.getFileName(experiment)).andReturn("test-files/qsar/exampleQsarResult.csv");
		
		List<QsarResult> qsarResults = new ArrayList<QsarResult>();
		qsarResults.add(new QsarResult(experiment,"C1CCC=CCC1","Training","-3.3405","-3.1", "-3.5", "-3.45"));
		qsarResults.add(new QsarResult(experiment,"NS(=O)(=O)c2cc1c(N=CNS1(=O)=O)cc2Cl","Test","-2.6219","-2.1", "-2.5", "-2.45"));
		qsarResults.add(new QsarResult(experiment,"CCCCCC#C","Training","-2.9734","-3.1", "-2.5", "-2.65"));
		qsarResults.add(new QsarResult(experiment,"O=C1CCCN1","Training","0.3054","0.1", "0.5", "0.45"));
		
		replay(databaseFiles);
		parser.parseFile();
		
		experiment.refresh();

		for(int i=0; i<qsarResults.size(); i++){
			assertEquals(qsarResults.get(i).molecule, experiment.results.get(i).molecule);
			assertEquals(qsarResults.get(i).partition, experiment.results.get(i).partition);
			assertEquals(qsarResults.get(i).experimental, experiment.results.get(i).experimental);
			assertEquals(qsarResults.get(i).fittedTrain, experiment.results.get(i).fittedTrain);
			assertEquals(qsarResults.get(i).looPrediction, experiment.results.get(i).looPrediction);
			assertEquals(qsarResults.get(i).fittedFull, experiment.results.get(i).fittedFull);
		}
		
		verify(databaseFiles);
	}

}
