package visitors;

import java.util.ArrayList;

import models.Deployment;
import models.MoleculeDatabase;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.Logger;
import utils.experiment.TestDataCreator;

public class MoleculeDatabaseCsvGeneratorTest extends FailingMethodsVisitorTest {
	
	MoleculeDatabaseCsvGenerator generator;
	MoleculeDatabase database;
	TestDataCreator dataCreator;
	User user;
	
	@Before
	public void setup(){
		user = new User("aperreau","hola","adeu");
		user.save();
		generator = new MoleculeDatabaseCsvGenerator();
		dataCreator = new TestDataCreator();
		ArrayList<String> propertyNames = new ArrayList<String>();
		propertyNames.add("apol");
		propertyNames.add("bpol");
		database = dataCreator.createSmallDatabaseWithPropertyNamesAndValues(user, propertyNames);
	}

	@Override
	public KnossosVisitor getVisitor() {
		return new MoleculeDatabaseCsvGenerator();
	}

	@Override
	public int[] getFailingTypes() {
		int[] failingTypes =  {MAPS_SIMILARITIES, PHYSICAL_SIMILARITIES, SCORING, 
				COMPARISON_EXPERIMENT, QSAR_EXPERIMENT, QSAR_RESULT};
		return failingTypes;
	}
	
	@Test
	public void moleculeDatabasetoCsvStringCorrectness(){
		generator.visit(database);
		String output = generator.getCsv();
		String trueOutput = "\"deploymentId\",\"MoleculeName\",\"apol\",\"bpol\"\n\"" + database.getAllDeployments().get(0).id + "\",\"probe1\",\"0\",\"1\"\n\"" + database.getAllDeployments().get(1).id + "\",\"probe2\",\"0\",\"1\"\n\"" + database.getAllDeployments().get(2).id + "\",\"probe3\",\"2\",\"3\"\n";
		assertEquals(trueOutput, output);
	}

	@Test
	public void moleculeDatabasetoCsvStringSortsByDeploymentOrder(){
		Deployment deployment1 = database.getAllDeployments().get(0);
		Deployment deployment2 = database.getAllDeployments().get(1);
		Deployment deployment3 = database.getAllDeployments().get(2);
		deployment1.putProperty("deploymentOrder", "1");
		deployment2.putProperty("deploymentOrder", "3");
		deployment3.putProperty("deploymentOrder", "2");
                database.save();                
		generator.visit(database);
		String output = generator.getCsv();
		String trueOutput = "\"deploymentId\",\"MoleculeName\",\"apol\",\"bpol\",\"deploymentOrder\"\n\"" + database.getAllDeployments().get(0).id + "\",\"probe1\",\"0\",\"1\",\"1\"\n\"" + database.getAllDeployments().get(2).id + "\",\"probe3\",\"2\",\"3\",\"2\"\n\"" + database.getAllDeployments().get(1).id + "\",\"probe2\",\"0\",\"1\",\"3\"\n";
		assertEquals(trueOutput, output);
	}
	
}
