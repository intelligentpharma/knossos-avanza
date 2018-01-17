package json;

import java.io.File;
import java.util.List;

import models.ComparisonExperiment;
import models.Ponderation;
import models.PonderationTest;
import models.QsarExperiment;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;

public class ComparisonExperimentJsonImporterTest extends UnitTest{

	User owner;
	ComparisonExperiment simpleExperiment, evaluatedExperiment;
	QsarExperiment qsarExperiment;
	List<Ponderation> simpleExperimentPonderations, evaluatedExperimentPonderations;
	
	@Before
	public void importExperiments(){
		ComparisonExperimentJsonImporter jsonImporter = new ComparisonExperimentJsonImporter();
		owner = new User("test", "test", "test");
		jsonImporter.setOwner(owner);
		
		File simpleExperimentfile = new File("test-files/json/simpleExperimentExport.json");

		
		jsonImporter.loadModelsFromJson(simpleExperimentfile);
		simpleExperiment = jsonImporter.getExperiment();
		qsarExperiment = jsonImporter.getQsarExperiment();
		simpleExperimentPonderations = jsonImporter.getPonderations();
		
		File evaluatedExperimentFile = new File("test-files/json/experiment1mol1dep1propEvaluatedExport.json");
		
		jsonImporter.loadModelsFromJson(evaluatedExperimentFile);
		evaluatedExperiment = jsonImporter.getExperiment();
		evaluatedExperimentPonderations = jsonImporter.getPonderations();
	}

	@Test
	public void experimentNamesAreCorrect(){
		assertEquals("Test experiment", simpleExperiment.name);
		assertEquals("Experiment 1mol1dep1prop", evaluatedExperiment.name);
	}

	@Test
	public void databaseNamesAreCorrect(){
		assertEquals("Salts", simpleExperiment.targetMolecules.name);
		assertEquals("Proteins", simpleExperiment.probeMolecules.name);
		assertEquals("Database 1mol-1dep-1prop", evaluatedExperiment.targetMolecules.name);
		assertEquals("Database 1mol-1dep-1prop", evaluatedExperiment.probeMolecules.name);
	}

	@Test
	public void numberOfParsedMoleculeIsCorrect(){
		assertEquals(0,simpleExperiment.probeMolecules.molecules.size());
		assertEquals(0,simpleExperiment.targetMolecules.molecules.size());
		assertEquals(1,evaluatedExperiment.probeMolecules.molecules.size());
		assertEquals(1,evaluatedExperiment.targetMolecules.molecules.size());
	}
	
	@Test
	public void numberOfParsedDeploymentIsCorrect(){
		assertEquals(0,simpleExperiment.probeMolecules.getAllDeployments().size());
		assertEquals(0,simpleExperiment.targetMolecules.getAllDeployments().size());
		assertEquals(1,evaluatedExperiment.probeMolecules.getAllDeployments().size());
		assertEquals(1,evaluatedExperiment.targetMolecules.getAllDeployments().size());
	}

	@Test
	public void numberOfParsedChemicalPropertiesIsCorrect(){
		assertEquals(1,evaluatedExperiment.probeMolecules.getAllDeployments().get(0).properties.size());
		assertEquals(1,evaluatedExperiment.targetMolecules.getAllDeployments().get(0).properties.size());
	}
	
	@Test
	public void numberOfAlignmentsParsedIsCorrect(){
		assertEquals(0,simpleExperiment.alignments.size());
		assertEquals(1,evaluatedExperiment.alignments.size());
	}
	
	@Test
	public void numberOfScoringsParsedIsCorrect(){
		assertEquals(1, evaluatedExperiment.alignments.get(0).scorings.size());
	}
	
	@Test
	public void scoreIsCorrect(){
		assertEquals(17.5722, evaluatedExperiment.alignments.get(0).scorings.get(0).score, 0.001);
	}
	
	@Test
	public void numberOfPonderationsIsCorrect(){
		assertEquals(0, simpleExperimentPonderations.size());
		assertEquals(1, evaluatedExperimentPonderations.size());
	}

	@Test
	public void ponderationsAreParsedCorrectly(){
		Ponderation ponderation = evaluatedExperimentPonderations.get(0);
		PonderationTest.assertInverseADPonderationWeightsAreCorrect(ponderation);		
	}
	
	@Test
	public void referencesFromMoleculeToMoleculeDatabaseAreRestored(){
		assertEquals(evaluatedExperiment.targetMolecules, 
				evaluatedExperiment.targetMolecules.molecules.get(0).database);
	}
	
	@Test
	public void referencesFromDeploymentToMoleculeAreRestored(){
		assertEquals(evaluatedExperiment.targetMolecules.molecules.get(0), 
				evaluatedExperiment.targetMolecules.getAllDeployments().get(0).molecule);
	}
	
	@Test
	public void referencesFromChemicalPropertyToDeploymentAreRestored(){
		assertEquals(evaluatedExperiment.targetMolecules.getAllDeployments().get(0),
				evaluatedExperiment.targetMolecules.getAllDeployments().get(0).properties.get(0).deployment);
	}

	@Test
	public void referencesFromScoringToAlignmentAreRestored(){
		assertEquals(evaluatedExperiment.targetMolecules.getAllDeployments().get(0),
				evaluatedExperiment.targetMolecules.getAllDeployments().get(0).properties.get(0).deployment);		
	}
	
	@Test 
	public void referencesFromAlignmentToExperimentAreRestored(){
		assertEquals(evaluatedExperiment, evaluatedExperiment.alignments.get(0).experiment);
	}

	@Test 
	public void referencesFromAlignmentToDeploymentAreRestored(){
		assertEquals(evaluatedExperiment.targetMolecules.getAllDeployments().get(0),
				evaluatedExperiment.alignments.get(0).targetDeployment);
		assertEquals(evaluatedExperiment.probeMolecules.getAllDeployments().get(0),
				evaluatedExperiment.alignments.get(0).probeDeployment);
	}
	
	@Test
	public void referencesFromScoringToPonderationAreRestored(){
		assertEquals(evaluatedExperimentPonderations.get(0), 
				evaluatedExperiment.alignments.get(0).scorings.get(0).ponderation);
	}

	@Test 
	public void ownerIsSetCorrectly(){
		assertEquals(owner, simpleExperiment.owner);
		assertEquals(owner, simpleExperiment.targetMolecules.owner);
		assertEquals(owner, simpleExperiment.probeMolecules.owner);

		assertEquals(owner, evaluatedExperiment.owner);
		assertEquals(owner, evaluatedExperiment.targetMolecules.owner);
		assertEquals(owner, evaluatedExperiment.probeMolecules.owner);
		assertEquals(owner, evaluatedExperimentPonderations.get(0).owner);

	}

	@Test
	public void qsarExperimentIsNull(){
		assertNull(qsarExperiment);
	}
	
}
