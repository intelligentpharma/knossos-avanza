package files.formats.csv;

import static org.easymock.EasyMock.*;
import models.QsarExperiment;
import models.User;

import org.junit.Before;
import org.junit.Test;

import files.DatabaseFiles;
import files.formats.csv.QsarExperimentCsvParser;
import files.formats.csv.QsarExperimentCsvParserEmptyImpl;

import play.test.UnitTest;
import utils.experiment.TestDataCreator;

public class QsarExperimentCsvParserEmptyTest extends UnitTest{
	
	QsarExperimentCsvParser parser;
	QsarExperiment experiment;
	User user;
	TestDataCreator dataCreator;
	DatabaseFiles databaseFiles;
	
	@Before
	public void setup() {
		user = new User("aperreau","hola","adeu");
		user.save();
		dataCreator = new TestDataCreator();
		experiment = dataCreator.createQsarExperiment(user);
		databaseFiles = createMock(DatabaseFiles.class);
		parser = new QsarExperimentCsvParserEmptyImpl(experiment);
		parser.setDatabaseFiles(databaseFiles);
	}
	
	@Test
	public void parseDoesNothing(){
		replay(databaseFiles);
		parser.parseFile();
		verify(databaseFiles);
	}


}
