package utils.database;

import static org.easymock.EasyMock.*;

import java.io.File;

import models.MoleculeDatabase;
import models.User;

import org.junit.BeforeClass;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;
import utils.database.DatabasePopulationUtilsImpl;
import files.DatabaseFiles;
import files.MoleculeParser;
import files.formats.sdf.MoleculeParserSDFException;

public class DatabasePopulationUtilsTest extends UnitTest {

	static DatabasePopulationUtilsImpl utils;

	@BeforeClass
	public static void setup(){
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		utils = new DatabasePopulationUtilsImpl();
	}
	
	@Test
	public void createMoleculeDatabaseCorrectly(){
		try
		{
			Factory tools = createMock(Factory.class);
			MoleculeParser parser = createMock(MoleculeParser.class);
			String file = "test-files/mol12_13_test.sdf";
			expect(tools.getMoleculeFileParser(file)).andReturn(parser);
			utils.setToolsFactory(tools);
			parser.setMoleculeDatabase(anyObject(MoleculeDatabase.class));
			parser.parseFileAndLoadMolecules();
	
			DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
			dbFiles.store(anyObject(MoleculeDatabase.class), anyObject(File.class));
			utils.setDatabaseFiles(dbFiles);
			
			replay(tools, parser, dbFiles);
			User user = User.findByUserName("aperreau");
			MoleculeDatabase database = utils.createMoleculeDatabase("dbname", file, user);
	
			assertNotNull(database);
			assertEquals("dbname",database.name);
			assertEquals(file,database.originalFileName);
			assertEquals("aperreau",database.owner.username);
			
			verify(tools, parser, dbFiles);
		}
		catch (MoleculeParserSDFException E)
		{
			//Do nothing, loaded DBs will not have non-allowed chats
		}
	}

	//Not sure this is ever going to be needed. To afraid to erase it yet.
//	@Test
//	public void singleDeploymentDatabasesAreParsedCorrectly() {
//		User owner = User.findByUserName("aperreau");
//		MoleculeDatabase moleculaPetita = utils.createMoleculeDatabase("SingleCorina", "test-files/acceptance/superrapid/moleculapetita.sdf", owner);
//		assertEquals(1, moleculaPetita.molecules.size());
//	}
	
//	@Test
//	public void databaseWithoutCalculatedPropertiesHasCalculatedPropertiesFlagUnset() {
//		User owner = User.findByUserName("aperreau");
//		MoleculeDatabase database = utils.createMoleculeDatabase("4molec", "test-files/4_molec_orderTest.sdf", owner);
//		assertFalse(database.calculatedProperties);
//	}

	
}
