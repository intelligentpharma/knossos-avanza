package files.formats.csv;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.List;

import models.Deployment;
import models.MoleculeDatabase;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import utils.experiment.TestDataCreator;
import engine.factory.FileNameFactory;
import files.formats.csv.DatabaseActionOutputCsvParser;
import files.formats.csv.MoleculeDatabaseCsvParser;

public class MoleculeDatabaseCsvParserTest extends UnitTest{

	DatabaseActionOutputCsvParser parser;
	User user;
	FileNameFactory factory;
	TestDataCreator dataCreator;
	MoleculeDatabase database;
	
	@Before
	public void setup() {
		user = new User("aperreau","hola","adeu");
		user.save();
		dataCreator = new TestDataCreator();
		database = dataCreator.createSingleMoleculeDatabase(user); 
		factory = createNiceMock(FileNameFactory.class);
		parser = new MoleculeDatabaseCsvParser("test-files/qsar/preprocess", factory);
		parser.setDatabase(database);
	}
	
	@Test
	public void parseMoleculeDatabaseFileCorrectly(){
		expect(factory.getMoleculeDatabaseCsvFileName(database.id)).andReturn("/qsarDescriptorsCorrelatedProp.csv");
		replay(factory);
		parser.parseFileAndUpdate();
		verify(factory);
		List<Deployment> deployments = parser.getData();
		assertEquals(6,deployments.size());
		assertEquals(3,deployments.get(0).properties.size());
		assertEquals("0.4",deployments.get(0).getPropertyValue("cpol"));
		assertEquals("1.6",deployments.get(1).getPropertyValue("cpol"));
		assertEquals("13",deployments.get(2).getPropertyValue("cpol"));
		assertEquals("19.2",deployments.get(3).getPropertyValue("cpol"));
		assertEquals("9.8",deployments.get(4).getPropertyValue("cpol"));
		assertEquals("5.2",deployments.get(5).getPropertyValue("cpol"));
		assertEquals("0.2",deployments.get(0).getPropertyValue("apol"));
		assertEquals("0.8",deployments.get(1).getPropertyValue("apol"));
		assertEquals("6.5",deployments.get(2).getPropertyValue("apol"));
		assertEquals("9.6",deployments.get(3).getPropertyValue("apol"));
		assertEquals("4.9",deployments.get(4).getPropertyValue("apol"));
		assertEquals("2.6",deployments.get(5).getPropertyValue("apol"));
		assertEquals("1.3",deployments.get(0).getPropertyValue("bpol"));
		assertEquals("0.5",deployments.get(1).getPropertyValue("bpol"));
		assertEquals("0.8",deployments.get(2).getPropertyValue("bpol"));
		assertEquals("0.4",deployments.get(3).getPropertyValue("bpol"));
		assertEquals("5.4",deployments.get(4).getPropertyValue("bpol"));
		assertEquals("6.7",deployments.get(5).getPropertyValue("bpol"));
	}
	
	@Test
	public void parsePropertiesWithCommas(){
		expect(factory.getMoleculeDatabaseCsvFileName(database.id)).andReturn("/qsarDescriptorsPropertiesWithComas.csv");
		replay(factory);
		parser.parseFileAndUpdate();
		verify(factory);
		List<Deployment> deployments = parser.getData();
		assertEquals(1,deployments.size());
		assertEquals(3,deployments.get(0).properties.size());
		assertEquals("0.4",deployments.get(0).getPropertyValue("cpol"));
		assertEquals("0.2",deployments.get(0).getPropertyValue("apol"));
		assertEquals("hola,bpol",deployments.get(0).getPropertyValue("bpol"));
	}

}
