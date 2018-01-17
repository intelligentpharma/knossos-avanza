package json;

import java.io.File;

import models.ComparisonExperiment;
import models.QsarExperiment;
import models.QsarResult;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;

public class QsarExperimentJsonImporterTest extends UnitTest{

	User owner;
	QsarExperiment simpleExperiment, evaluatedExperiment;
	ComparisonExperiment comparisonExperiment;
	
	@Before
	public void importExperiments(){
		QsarExperimentJsonImporter jsonImporter = new QsarExperimentJsonImporter();
		owner = new User("test", "test", "test");
		jsonImporter.setOwner(owner);
		
		File simpleExperimentfile = new File("test-files/json/simpleQsarExperiment.json");
		
		jsonImporter.loadModelsFromJson(simpleExperimentfile);
		simpleExperiment = jsonImporter.getQsarExperiment();
		comparisonExperiment = jsonImporter.getExperiment();
		
		File evaluatedExperimentFile = new File("test-files/json/qsarExperimentImport.json");
		
		jsonImporter.loadModelsFromJson(evaluatedExperimentFile);
		evaluatedExperiment = jsonImporter.getQsarExperiment();
	}
	
	@Test
	public void experimentNamesAreCorrect(){
		assertEquals("Experiment 1mol1dep1prop", simpleExperiment.name);
		assertEquals("provaPlsModel", evaluatedExperiment.name);
	}
	
	@Test
	public void databaseNamesAreCorrect(){
		assertEquals("Database 1mol-1dep-1prop", simpleExperiment.molecules.name);
		assertEquals("testPLSMini", evaluatedExperiment.molecules.name);
	}
	
	@Test
	public void numberOfParsedMoleculeIsCorrect(){
		assertEquals(1,simpleExperiment.molecules.molecules.size());
		assertEquals(0,evaluatedExperiment.molecules.molecules.size());
	}
	
	@Test
	public void numberOfParsedDeploymentIsCorrect(){
		assertEquals(1,simpleExperiment.molecules.getAllDeployments().size());
		assertEquals(0,evaluatedExperiment.molecules.getAllDeployments().size());
	}
	
	@Test
	public void numberOfParsedChemicalPropertiesIsCorrect(){
		assertEquals(1,simpleExperiment.molecules.getAllDeployments().get(0).properties.size());
	}

	@Test
	public void numberOfQsarResultsAreCorrect(){
		assertEquals(0,simpleExperiment.results.size());
		assertEquals(3,evaluatedExperiment.results.size());
	}
	
	@Test
	public void qsarResultsAreCorrect(){
		QsarResult result1 = evaluatedExperiment.results.get(0);
		assertEquals("Training",result1.partition);
		assertEquals("7.443",result1.experimental);
		assertEquals("7.4495",result1.fittedTrain);
		assertEquals("5.1704",result1.looPrediction);
		assertEquals("7.4494",result1.fittedFull);
		assertEquals("Clc1c(Cl)c(Cl)c(c(Cl)c1Cl)c2c(Cl)c(Cl)c(Cl)c(Cl)c2Cl",result1.molecule);
		
		QsarResult result2 = evaluatedExperiment.results.get(1);
		assertEquals("Training",result2.partition);
		assertEquals("4.4658",result2.experimental);
		assertEquals("4.4653",result2.fittedTrain);
		assertEquals("3.6204",result2.looPrediction);
		assertEquals("4.4638",result2.fittedFull);
		assertEquals("Clc1cccc(c1Cl)c2c(Cl)c(Cl)cc(Cl)c2Cl",result2.molecule);

		QsarResult result3 = evaluatedExperiment.results.get(2);
		assertEquals("Training",result3.partition);
		assertEquals("0.4562",result3.experimental);
		assertEquals("0.4574",result3.fittedTrain);
		assertEquals("0.2555",result3.looPrediction);
		assertEquals("0.4545",result3.fittedFull);
		assertEquals("C1c2ccccc2c3cc4ccccc4cc13",result3.molecule);
	}
	
	@Test
	public void referencesFromMoleculeToMoleculeDatabaseAreRestored(){
		assertEquals(simpleExperiment.molecules, simpleExperiment.molecules.molecules.get(0).database);
	}
	
	@Test
	public void referencesFromDeploymentToMoleculeAreRestored(){
		assertEquals(simpleExperiment.molecules.molecules.get(0),simpleExperiment.molecules.getAllDeployments().get(0).molecule);
	}
	
	@Test
	public void referencesFromChemicalPropertyToDeploymentAreRestored(){
		assertEquals(simpleExperiment.molecules.getAllDeployments().get(0), simpleExperiment.molecules.getAllDeployments().get(0).properties.get(0).deployment);
	}

	@Test 
	public void ownerIsSetCorrectly(){
		assertEquals(owner, simpleExperiment.owner);
		assertEquals(owner, simpleExperiment.molecules.owner);

		assertEquals(owner, evaluatedExperiment.owner);
	}

	@Test
	public void comparisonExperimentIsNull(){
		assertNull(comparisonExperiment);
	}


}
