package files.formats.pdbqt;

import java.io.File;
import java.util.List;

import junitx.framework.FileAssert;
import models.Deployment;
import models.Molecule;
import models.MoleculeDatabase;
import models.User;

import org.junit.Before;
import org.junit.Test;

import files.formats.pdbqt.MoleculeParserPdbqt;

import play.test.UnitTest;

public class MoleculeParserPdbqtTest extends UnitTest {

	User user;
	MoleculeDatabase database;
	MoleculeParserPdbqt parser;

	@Before
	public void setup() {
		database = new MoleculeDatabase();
		database.name = "mol30";
		database.transientFile = new File("test-files/test2.pdbqt");
		parser = new MoleculeParserPdbqt();
		
		parser.setMoleculeDatabase(database);
		parser.parseFileAndLoadMolecules();
	}

	@Test
	public void thereIsJustOneMolecule() {
		assertEquals(1, database.molecules.size());
	}

	@Test
	public void specificMoleculeCanBeFound() {
		assertNotNull(parser.findMoleculeByName("mol30"));
	}

	//SAME
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
	public void thereAreNoProperties(){
		List<String> propertyNames = database.getPropertyNames();
		assertEquals(0,propertyNames.size());
		for(Deployment deployment : database.getAllDeployments()){
			assertEquals(0, deployment.properties.size());
		}
	}

	//SAME
	@Test
	public void moleculesAreLinkedToDataBase(){
		List<Molecule> molecules = database.molecules;
		for(Molecule molecule : molecules){
			assertNotNull(molecule.database);
		}
	}

	@Test
	public void theDeploymentFileIsTheDatabaseFile(){
		FileAssert.assertEquals(database.transientFile, database.getAllDeployments().get(0).transientFile);
	}

	@Test
	public void allDeploymentsHavePdbqtType(){
		for(Deployment deployment : database.getAllDeployments()){
			assertEquals("PDBQT", deployment.type);
		}
	}
	
	@Test
	public void thereIsJustOneDeployment(){
		assertEquals(1, database.getAllDeployments().size());
	}
	
	@Test
	public void deploymentAndMoleculeHaveDatabaseName(){
		for(Molecule molecule : database.molecules){
			assertEquals(database.name, molecule.name);
			for(Deployment deployment : molecule.deployments){
				assertEquals(database.name, deployment.name);
			}
		}
	}
}

