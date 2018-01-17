package models;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.experiment.TestDataCreator;

public class ChemicalPropertiesTest extends UnitTest {

	Molecule molecule1,molecule2,molecule3,molecule4,molecule5,molecule6;
	MoleculeDatabase database, database2;

	@Before
	public void setup() {

		database = new MoleculeDatabase();
		database2 = new MoleculeDatabase();
		
		molecule1 = new Molecule();
		molecule1.database = database;
		Deployment deployment1 = new Deployment();
		deployment1.name = "dep1";
		deployment1.putProperty("prop1", "propV");
		deployment1.putProperty("prop2", "propV");
		molecule1.addDeployment(deployment1);

		molecule2 = new Molecule();
		molecule2.database = database;
		Deployment deployment2 = new Deployment();
		deployment2.name = "dep2";
		deployment2.putProperty("prop21", "propV21");
		deployment2.putProperty("prop22", "propV22");
		molecule2.addDeployment(deployment2);

		molecule3 = new Molecule();
		molecule3.database = database;
		Deployment deployment3 = new Deployment();
		deployment3.name = "dep3";
		deployment3.putProperty("prop1", "propV");
		deployment3.putProperty("prop2", "propV");
		molecule3.addDeployment(deployment3);

		molecule4 = new Molecule();
		molecule4.database = database;
		Deployment deployment4 = new Deployment();
		deployment4.name = "dep4";
		deployment4.putProperty("prop1", "propV");
		molecule4.addDeployment(deployment4);

		molecule5 = new Molecule();
		molecule5.database = database;
		Deployment deployment5 = new Deployment();
		deployment5.name = "dep5";
		deployment5.putProperty("prop1", "propV");
		molecule5.addDeployment(deployment5);
		Deployment deployment51 = new Deployment();
		deployment51.name = "dep51";
		deployment51.putProperty("prop2", "propV");
		molecule5.addDeployment(deployment51);
		
		molecule6 = new Molecule();
		molecule6.database = database;
		Deployment deployment6 = new Deployment();
		deployment6.name = "dep6";
		String propVal = "a";
		for(int i=0; i<4010; i++){
			propVal+="a";
		}
		deployment6.putProperty("prop1", propVal);
		molecule6.addDeployment(deployment6);
	}

	@Test
	public void getPropertyNamesReturnsAllDeploymentPropertyNamesOfOneDatabase(){
		database.addMolecule(molecule1);
		database.addEmptyProperties();

		assertNotNull(database.getPropertyNames());
		assertEquals(2, database.getPropertyNames().size());
	}
	
	@Test
	public void getPropertyNamesDistinguishesPropertiesDeploymentsOfDiferentDatabases(){
		database.addMolecule(molecule1);
		database2.addMolecule(molecule2);

		database.addEmptyProperties();
		database2.addEmptyProperties();
		
		assertEquals(2, database.getPropertyNames().size());
	}

	@Test
	public void ifAllDeploymentsHaveSamePropertiesThereAreNoDuplicates(){
		database.addMolecule(molecule1);
		database.addMolecule(molecule3);

		database.addEmptyProperties();
		
		assertEquals(2, database.getPropertyNames().size());
	}

	@Test
	public void ifAllDeploymentsHaveDifferentPropertiesAllNamesAreReturned(){
		database.addMolecule(molecule1);
		database.addMolecule(molecule2);

		database.addEmptyProperties();
		
		assertEquals(4, database.getPropertyNames().size());
	}

	@Test
	public void allDeploymentsOfTheSameMoleculeAreIterated(){
		database.addMolecule(molecule4);
		database.addMolecule(molecule5);

		database.addEmptyProperties();

		assertEquals(2, database.getPropertyNames().size());
	}

	@Test
	public void emptyListReturnedWhenNoDeployments(){
		assertNotNull(database.getPropertyNames());
		assertEquals(0, database.getPropertyNames().size());
	}
	
	@Test
	public void largePropertiesAreTrunked(){	
		String propVal = molecule6.deployments.get(0).getPropertyValue("prop1");
		
		Logger.info("Test Property Value %d",propVal.length());
		
		String propValTrunkated = "a";
		for(int i=0; i<3997; i++){
			propValTrunkated+="a";
		}
		assertEquals(propValTrunkated,propVal);
	}
	
	@Test
	public void getChemicalPropertyByDeploymentIdAndName(){
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		TestDataCreator creator = new TestDataCreator();
		User user = User.findByUserName("xmaresma");
		List<String> properties = new ArrayList<String>();
		properties.add("property1");
		properties.add("property2");
		MoleculeDatabase database = creator.createSmallDatabaseWithProperties(user, properties);
		
		ChemicalProperty property = ChemicalProperty.findByDeploymentAndName(database.molecules.get(0).deployments.get(0), "property1");
		assertEquals(property.name, "property1");
		assertEquals(database.molecules.get(0).deployments.get(0).id, property.deployment.id);
		assertEquals("don't care", property.value);
	}

}
