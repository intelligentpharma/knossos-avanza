package files.formats.csv;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import models.QsarExperiment;
import models.User;

import org.junit.Before;
import org.junit.Test;

import files.DatabaseFiles;
import files.formats.csv.QsarExperimentCsvParserCoefficient;

import play.test.UnitTest;
import utils.experiment.TestDataCreator;

public class QsarExperimentCsvParserCoeficientsTest extends UnitTest {
	
	QsarExperimentCsvParserCoefficient parser;
	QsarExperiment experiment;
	DatabaseFiles databaseFiles;
	TestDataCreator dataCreator;
	User user;

	@Before
	public void setup() {
		user = new User("aperreau","hola","adeu");
		user.save();
		dataCreator = new TestDataCreator();
		experiment = dataCreator.createQsarExperiment(user);
		databaseFiles = createMock(DatabaseFiles.class);
		String[] boundaries = {"5","4","1"};
		parser = new QsarExperimentCsvParserCoefficient(experiment.id,boundaries);
		parser.setDatabaseFiles(databaseFiles);
	}
	
	@Test
	public void getCoefficientsFromQsarExperimentWithOneGrindRemoved(){
		expect(databaseFiles.getPath(experiment)).andReturn("test-files/qsar/coefficients");
		replay(databaseFiles);
		parser.parseFile();
		verify(databaseFiles);
		
		String expectedCoefficients = "0.0145,0,0.0172,0.0182,0.0181,0.0168,0.0157,0.0126,0.0118,0.0114";
		String parsedCoefficients = parser.extractCoefficients();
		assertEquals(expectedCoefficients, parsedCoefficients);		
	}
	
	

}
