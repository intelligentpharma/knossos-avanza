package models;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.experiment.TestDataCreator;

public class DeploymentPropertiesTest extends UnitTest {

	static User user;
	static Deployment deployment;
	static MoleculeDatabase db;

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		user = User.findByUserName("aperreau");
		deployment = new Deployment();
		TestDataCreator creator = new TestDataCreator();
		db = creator.createSmallDatabaseWithoutProperties(user);
	}

	@Test
	public void newDeploymentHasNoProperties(){
		Deployment deployment = new Deployment();
		assertNotNull(deployment.properties);
		assertTrue(deployment.properties.isEmpty());
	}
	
	@Test
	public void newPropertyIsStoredCorrectly(){
		Deployment deployment = db.getAllDeployments().get(0);
		deployment.putProperty("name", "value");
		assertEquals(1, deployment.properties.size());
	}
	
	@Test
	public void existingPropertyIsOverwritten(){
		Deployment deployment = db.getAllDeployments().get(0);
		deployment.putProperty("name", "value");
		deployment.putProperty("name", "newValue");
		assertEquals(1, deployment.properties.size());
	}
	
	@Test
	public void propertiesAreRetrievedFromDB(){
		Deployment deployment = db.getAllDeployments().get(0);
		deployment.putProperty("name1", "value1");
		deployment.putProperty("name2", "value2");
		deployment.putProperty("name2", "newValue2");
		
		db.save();
		
		Deployment savedDeployment = Deployment.findById(deployment.id);
		assertEquals(2, savedDeployment.properties.size());
		assertEquals("newValue2", savedDeployment.getPropertyValue("name2"));
	}
	
}
