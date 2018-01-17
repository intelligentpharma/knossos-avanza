package files.formats.sdf;

import java.io.File;
import java.util.List;

import models.ChemicalProperty;
import models.Deployment;
import models.Molecule;
import models.MoleculeDatabase;
import models.User;

import org.junit.Before;
import org.junit.Test;

import files.DatabaseFiles;
import files.DatabaseFilesImpl;
import files.FileReadingMoleculeParser;
import files.formats.sdf.MoleculeParserSDFException;
import files.formats.sdf.MoleculeParserSdf;

import play.test.UnitTest;

public class MoleculeParserSdfTest extends UnitTest {

	User user;
	MoleculeDatabase database;
	FileReadingMoleculeParser parserSdf;
	DatabaseFiles dbFiles;

	@Before
	public void setup() {
		parserSdf = new MoleculeParserSdf();
		dbFiles = new DatabaseFilesImpl();
		database = new MoleculeDatabase();
		
		database.name = "mol30";
		database.transientFile = new File("test-files/test2.sdf");
			
		parserSdf.setMoleculeDatabase(database);
		parserSdf.setDatabaseFiles(dbFiles);
		try{
		parserSdf.parseFileAndLoadMolecules();
		}
		catch (MoleculeParserSDFException E){
			//Do nothing, loaded DBs will not have non-allowed chats
		}
	}

	@Test
	public void correctNumberOfMoleculesCreated() {
		assertEquals(2, database.molecules.size());
	}

	@Test
	public void correctNumberOfDeploymentsCreated() {
		int deployments = 0;
		for(Molecule molecule: database.molecules) {
			assertEquals(1, molecule.deployments.size());
			deployments += molecule.deployments.size();
		}
		assertEquals(2, deployments);
	}

	@Test
	public void specificMoleculeCanBeFound() {
		assertNotNull(parserSdf.findMoleculeByName("testName_mol12"));
	}

	@Test
	public void allDeploymentsHaveCorrectReferences() {
		for(Molecule molecule : database.molecules) {
			verifyDeploymentsAreLinked(molecule);
		}
	}

	private void verifyDeploymentsAreLinked(Molecule molecule) {
		for (Deployment deployment : molecule.deployments) {
			assertEquals(molecule, deployment.molecule);
		}
	}

	@Test
	public void allPropertiesAreSaved(){
		List<Molecule> molecules = database.molecules;
		List<String> propertyNames = database.getPropertyNames();
		assertEquals(7,propertyNames.size());
		assertEquals(7,molecules.get(0).deployments.get(0).properties.size());
		assertEquals(7, molecules.get(1).deployments.get(0).properties.size());
	}

	@Test
	public void differentNameTagFormatsAreParsedCorrectly(){
		List<Molecule> molecules = database.molecules;
		List<String> propertyNames = database.getPropertyNames();
		assertTrue(propertyNames.contains("INVERS"));
		assertNotNull(molecules.get(1).deployments.get(0).getPropertyValue("INVERS"));
	}

	@Test
	public void replaceOfTagCOMPNDDoneCorrectly(){
		List<Molecule> molecules = database.molecules;
		List<String> propertyNames = database.getPropertyNames();
		assertTrue(propertyNames.contains("COMP01"));
		assertNotNull(molecules.get(1).deployments.get(0).getPropertyValue("COMP01"));
	}

	@Test
	public void PropertiesWithMoreThanOneLineAreSavedCorrectly(){
		List<Molecule> molecules = database.molecules;
		assertEquals("VINA RESULT:     -10.7      0.000      0.000\n  4 active torsions:\n  "
				+ "status: ('A' for Active; 'I' for Inactive)\n    1  A    between atoms: C_2  and  C_3\n    "
				+ "2  A    between atoms: C_3  and  N_4\n    3  A    between atoms: N_4  and  C_5\n    "
				+ "4  A    between atoms: C_5  and  C_6",molecules.get(0).deployments.get(0).getPropertyValue("REMARK"));
	}

	@Test
	public void deploymentsAreLinkedWithProperties(){
		List<Molecule> molecules = database.molecules;
		for(Deployment deployment : molecules.get(0).deployments){
			for(ChemicalProperty property : deployment.properties){
				assertEquals(deployment,property.deployment);
			}
			assertNotSame(0, deployment.properties.size());
		}
	}

	@Test
	public void moleculesAreLinkedToDataBase(){
		List<Molecule> molecules = database.molecules;
		for(Molecule molecule : molecules){
			assertNotNull(molecule.database);
		}
	}

	@Test
	public void allDeploymentHaveFiles(){
		for(Deployment deployment : database.getAllDeployments()){
			assertNotNull(deployment.transientFile);
		}
	}

	@Test
	public void allDeploymentsHavePdbqtType(){
		for(Deployment deployment : database.getAllDeployments()){
			assertEquals("SDF", deployment.type);
		}
	}

	@Test
	public void onlyDeploymentsWithNameAreStored(){
		parserSdf = new MoleculeParserSdf();
		dbFiles = new DatabaseFilesImpl();
		database = new MoleculeDatabase();
		
		database.name = "2deployments1withoutName";
		database.transientFile = new File("test-files/SDFparser/2deployments1withoutName.sdf");
			
		parserSdf.setMoleculeDatabase(database);
		parserSdf.setDatabaseFiles(dbFiles);
		try{
		parserSdf.parseFileAndLoadMolecules();}
		catch (MoleculeParserSDFException E){
			//Do nothing, loaded DBs will not have non-allowed chats
		}
		
	}
	
	
}

