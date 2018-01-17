package models;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.experiment.TestDataCreator;

public class GrindVectorTest extends UnitTest{

	MoleculeDatabase database;
	Deployment deployment;
	User user;
	TestDataCreator dataCreator;
	GrindVector grindVector;
	
	@Before
    public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		user = User.findByUserName("xmaresma");
		deployment = new Deployment();
		dataCreator = new TestDataCreator();
		database = dataCreator.createSingleMoleculeDatabase(user);
		deployment = database.getAllDeployments().get(0);
		
		grindVector = new GrindVector("1,2,3","1,2,3","3,2,1",deployment.id,database.id,"0,0,0",0.8f);
		grindVector.setBoundaries("2,2,2");
		grindVector.save();
    }
	
	@Test
	public void grindVectorToString(){
		assertEquals(grindVector.toString(),"1,2,3|1,2,3|3,2,1|2,2,2|0,0,0");
	}
	
	@Test
	public void findByDeploymentId(){
		GrindVector newGrindVector = GrindVector.findByDeploymentId(deployment.id);
		assertEquals(grindVector.toString(), newGrindVector.toString());
	}
	
	@Test
	public void findByMoleculeDatabaseId(){
		List<GrindVector> grindVectorList = GrindVector.findByMoleculeDatabaseId(database.id);	
		assertEquals(grindVector.toString(), grindVectorList.get(0).toString());
	}
	
}
