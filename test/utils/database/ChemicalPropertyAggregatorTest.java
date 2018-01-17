package utils.database;

import java.util.ArrayList;
import java.util.List;

import models.ChemicalProperty;
import models.Deployment;
import models.MoleculeDatabase;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.database.ChemicalPropertyAggregator;
import utils.experiment.TestDataCreator;

public class ChemicalPropertyAggregatorTest extends UnitTest{

	ChemicalPropertyAggregator aggregator;
	TestDataCreator creator;
	User user;
	List<String> properties;
	
	@Before
	public void setUp(){
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		user = User.findByUserName("xmaresma");
		creator = new TestDataCreator();
		properties = new ArrayList<String>();
		properties.add("property1");
		properties.add("property2");
		properties.add("property3");
		properties.add("property4");		
		aggregator = new ChemicalPropertyAggregator();
	}
	
	@Test
	public void oldPropertyFullIsNotModified(){
		MoleculeDatabase database = creator.createSmallDatabaseWithPropertyNamesAndValues(user, properties);
		aggregator.setDatabase(database);
		aggregator.setDestinyProperty("property1");
		aggregator.setFormula("|property2|");
		aggregator.calculateChemicalProperty();
		
		List<Deployment> deployments = database.getAllDeployments();
		assertEquals(deployments.get(0).getPropertyValue("property1"),"0");
		assertEquals(deployments.get(1).getPropertyValue("property1"),"0");
		assertEquals(deployments.get(2).getPropertyValue("property1"),"2");
	}
	
	@Test
	public void oldPropertyWithHolesIsFullFilled(){
		MoleculeDatabase database = creator.createSmallDatabaseWithPropertyNamesAndValues(user, properties);
		List<String> values = new ArrayList<String>();
		values.add("");
		values.add("3.0");
		values.add("7.0");
		values.add("1.0");
		Deployment deployment =  creator.createDeploymentWithPropertyNamesAndValue("name", properties, values);
		database.molecules.get(0).addDeployment(deployment);
		database.save();
		aggregator.setDatabase(database);
		aggregator.setDestinyProperty("property1");
		aggregator.setFormula("|property2|");
		
		aggregator.calculateChemicalProperty();
		
		List<Deployment> deployments = database.getAllDeployments();
		assertEquals(deployments.get(3).getPropertyValue("property1"),"3.0");		
	}
	
	@Test
	public void newPropertyIsAddedCorrectlyInAllDeployments(){
		MoleculeDatabase database = creator.createSmallDatabaseWithPropertyNamesAndValues(user, properties);
		aggregator.setDatabase(database);
		aggregator.setDestinyProperty("newProperty");
		aggregator.setFormula("|property1| + |property2|");
		aggregator.calculateChemicalProperty();

		List<Deployment> deployments = database.getAllDeployments();
		for(Deployment deployment : deployments){
			ChemicalProperty property1 = ChemicalProperty.findByDeploymentAndName(deployment, "property1");
			ChemicalProperty property2 = ChemicalProperty.findByDeploymentAndName(deployment, "property2");
			ChemicalProperty newProperty = ChemicalProperty.findByDeploymentAndName(deployment, "newProperty");
			double expectedValue = new Double(property1.value).doubleValue() + new Double(property2.value).doubleValue();
			double newValue = new Double(newProperty.value).doubleValue();
			assertEquals(5,deployment.properties.size());
			assertEquals(expectedValue, newValue,0.001);
		}
	}
	
	@Test
	public void newPropertyValueIsEmptyIfSomeVariableIsEmpty(){
		MoleculeDatabase database = creator.createSmallDatabaseWithPropertyNamesAndValues(user, properties);
		Deployment newDeployment = new Deployment();
		newDeployment.name = "name";
		newDeployment.properties = new ArrayList<ChemicalProperty>();
		newDeployment.properties.add(new ChemicalProperty("property2","3.0",newDeployment));
		database.molecules.get(0).addDeployment(newDeployment);
		database.save();

		aggregator.setDatabase(database);
		aggregator.setDestinyProperty("newProperty");
		aggregator.setFormula("|property1| + |property2|");
		aggregator.calculateChemicalProperty();

		Deployment deployment = Deployment.findDeploymentsByDatabaseAndName(database, "name").get(0);
		ChemicalProperty newProperty = ChemicalProperty.findByDeploymentAndName(deployment, "newProperty");
		assertEquals("", newProperty.value);
	}
	
	
	
}
