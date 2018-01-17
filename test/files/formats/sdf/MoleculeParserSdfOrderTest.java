package files.formats.sdf;

import java.io.File;

import models.ChemicalProperty;
import models.Deployment;
import models.Molecule;
import models.MoleculeDatabase;
import models.User;

import org.junit.Before;
import org.junit.Test;

import files.DatabaseFiles;
import files.DatabaseFilesImpl;
import files.formats.sdf.MoleculeParserSDFException;
import files.formats.sdf.MoleculeParserSdf;

import play.test.UnitTest;

public class MoleculeParserSdfOrderTest extends UnitTest {

	User user;
	MoleculeDatabase database;
	MoleculeParserSdf parserSdf;
	DatabaseFiles dbFiles;

	@Before
	public void setup() {
		String databaseFile = "test-files/4_molec_orderTest.sdf";
		setupParser(databaseFile);
	}

	private void setupParser(String databaseFileName) {

		database = new MoleculeDatabase();
		database.transientFile = new File(databaseFileName);
		dbFiles = new DatabaseFilesImpl();
		parserSdf = new MoleculeParserSdf();
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
	public void moleculesHaveOrderProperty(){
		for (Molecule molecule: database.molecules){
			for(Deployment deployment: molecule.getDeployments()){
				assertNotNull(deployment.getPropertyValue(ChemicalProperty.MOLECULE_ORDER));
			}
		}
	}

	@Test
	public void moleculeOrderPropertyIsFilledCorrectly(){
		for (Molecule molecule: database.molecules){
			verifyMoleculeOrder(molecule, "-StructureA", "1");
			verifyMoleculeOrder(molecule, "-StructureB", "2");
			verifyMoleculeOrder(molecule, "-StructureC", "3");
			verifyMoleculeOrder(molecule, "-StructureD", "4");
		}
	}

	private void verifyMoleculeOrder(Molecule molecule, String name, String order) {
		if(molecule.name.equalsIgnoreCase(name)){
			for(Deployment deployment : molecule.getDeployments()){
				assertEquals(order, deployment.getPropertyValue(ChemicalProperty.MOLECULE_ORDER));
			}
		}
	}

	@Test
	public void deploymentsHaveOrderProperty(){
		for(Deployment deployment : database.getAllDeployments()){
			assertNotNull(deployment.getPropertyValue(ChemicalProperty.DEPLOYMENT_ORDER));
		}
	}

	@Test
	public void deploymentOrderPropertyIsFilledCorrectly(){
		for (Molecule molecule: database.molecules){
			verifyDeploymentOrder(molecule, "-StructureA", 1, 1, "1");
			verifyDeploymentOrder(molecule, "-StructureB", 1, 1, "2");
			verifyDeploymentOrder(molecule, "-StructureA", 1, 2, "3");
			verifyDeploymentOrder(molecule, "-StructureC", 1, 1, "4");
			verifyDeploymentOrder(molecule, "-StructureD", 1, 1, "5");
			verifyDeploymentOrder(molecule, "-StructureB", 1, 2, "6");
		}
	}

	private void verifyDeploymentOrder(Molecule molecule, String name, int isomer, int conformation, String order) {
		for(Deployment deployment: molecule.getDeployments()){
			if(molecule.name.equalsIgnoreCase(name) && deployment.isomer==isomer && deployment.conformation==conformation){
				assertEquals(order, deployment.getPropertyValue(ChemicalProperty.DEPLOYMENT_ORDER));							
			}
		}
	}
}