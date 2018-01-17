package utils.experiment;

import static org.easymock.EasyMock.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import json.ExperimentJsonImporter;
import models.Alignment;
import models.Deployment;
import models.ComparisonExperiment;
import models.MoleculeDatabase;
import models.Ponderation;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.db.jpa.JPA;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.CompressionUtils;
import utils.ModelUtils;
import utils.experiment.ExperimentImporter;
import files.DatabaseFiles;

public class ExperimentImporterTest extends UnitTest{
	
	ComparisonExperiment experiment;
	List<Ponderation> ponderations;
	File alignmentFile;
	File moleculeDatabaseFile;
	File deploymentFile;
	
	@Before
	public void setup(){
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		TestDataCreator dataCreator = new TestDataCreator();
		experiment = dataCreator.create1mol1dep1propEvaluatedExperiment();
		JPA.em().detach(experiment);
		ModelUtils.setAllModelIdsToGivenId(experiment, 666L);
		ponderations = experiment.getPonderations();
		for(Ponderation ponderation : ponderations){
			JPA.em().detach(ponderation);
		}
	}
	
	@Test
	public void setsOwnerToJsonImporter(){
		User owner = new User(null,null,null);
		ExperimentImporter importer = new ExperimentImporter();
		ExperimentJsonImporter jsonImporter = createMock(ExperimentJsonImporter.class);
		jsonImporter.setOwner(owner);
		importer.setJsonImporter(jsonImporter);
		replay(jsonImporter);
		
		importer.setOwner(owner);
		
		verify(jsonImporter);
	}
	
	@Test
	public void experimentIsLoadedFromJsonFileInGzip(){
		ExperimentImporter importer = new ExperimentImporter();
		
		CompressionUtils compressionUtils = createMock(CompressionUtils.class);
		compressionUtils.uncompress(anyObject(File.class),	anyObject(File.class));
		File jsonFile = new File("test-files/json/experiment1mol1dep1propEvaluatedExport.json");
		List<File> uncompressedFiles = getListOfRelatedFiles(jsonFile);
		expect(compressionUtils.untarFiles(anyObject(File.class))).andReturn(uncompressedFiles);
		
		ExperimentJsonImporter jsonImporter = createMock(ExperimentJsonImporter.class);
		jsonImporter.loadModelsFromJson(jsonFile);
		expect(jsonImporter.getExperiment()).andReturn(experiment);
		expect(jsonImporter.getPonderations()).andReturn(ponderations);
		
		DatabaseFiles databaseFiles = createNiceMock(DatabaseFiles.class);
		
		replay(compressionUtils, jsonImporter, databaseFiles);

		importer.setCompressionUtils(compressionUtils);
		importer.setDatabaseFiles(databaseFiles);
		importer.setJsonImporter(jsonImporter);
		
		File inputFile = new File("test-files/");
		JPA.setRollbackOnly();
		importer.importExperiment(inputFile);
		
		verify(compressionUtils, jsonImporter, databaseFiles);
	}

	@Test
	public void importedExperimentHasLinksToAllTransientFiles(){
		ExperimentImporter importer = new ExperimentImporter();
		
		CompressionUtils compressionUtils = createMock(CompressionUtils.class);
		compressionUtils.uncompress(anyObject(File.class),	anyObject(File.class));
		File jsonFile = new File("test-files/json/experiment1mol1dep1propEvaluatedExport.json");
		List<File> uncompressedFiles = getListOfRelatedFiles(jsonFile);
		expect(compressionUtils.untarFiles(anyObject(File.class))).andReturn(uncompressedFiles);
		
		ExperimentJsonImporter jsonImporter = createMock(ExperimentJsonImporter.class);
		jsonImporter.loadModelsFromJson(jsonFile);
		expect(jsonImporter.getExperiment()).andReturn(experiment);
		expect(jsonImporter.getPonderations()).andReturn(ponderations);
		
		DatabaseFiles databaseFiles = createNiceMock(DatabaseFiles.class);
		
		replay(compressionUtils, jsonImporter, databaseFiles);

		importer.setCompressionUtils(compressionUtils);
		importer.setDatabaseFiles(databaseFiles);
		importer.setJsonImporter(jsonImporter);
		
		File inputFile = new File("test-files/");
		JPA.setRollbackOnly();
		importer.importExperiment(inputFile);
		
		for(Alignment alignment: experiment.alignments){
			assertEquals(alignmentFile, alignment.transientFile);
		}
		verifyTransientFilesInMoleculeDatabase(experiment.targetMolecules);
		verifyTransientFilesInMoleculeDatabase(experiment.probeMolecules);
		
		verify(compressionUtils, jsonImporter, databaseFiles);
	}

	private void verifyTransientFilesInMoleculeDatabase(
			MoleculeDatabase molecules) {
		assertEquals(moleculeDatabaseFile, molecules.transientFile);
		for(Deployment deployment : molecules.getAllDeployments()){
			assertEquals(deploymentFile, deployment.transientFile);
		}
	}

	private List<File> getListOfRelatedFiles(File jsonFile) {
		List<File> uncompressedFiles = new ArrayList<File>();
		alignmentFile = new File("experiment/666/66/alignment_666");
		moleculeDatabaseFile = new File("db/666/database");
		deploymentFile = new File("db/666/66/deployment_666");
		uncompressedFiles.add(jsonFile);
		uncompressedFiles.add(alignmentFile);
		uncompressedFiles.add(moleculeDatabaseFile);
		uncompressedFiles.add(deploymentFile);
		uncompressedFiles.add(moleculeDatabaseFile);
		uncompressedFiles.add(deploymentFile);
		return uncompressedFiles;
	}
	
	@Test
	public void filesAreStoredToTheRightPlace(){
		ExperimentImporter importer = new ExperimentImporter();
		
		CompressionUtils compressionUtils = createMock(CompressionUtils.class);
		compressionUtils.uncompress(anyObject(File.class),	anyObject(File.class));
		File jsonFile = new File("test-files/json/experiment1mol1dep1propEvaluatedExport.json");
		List<File> uncompressedFiles = getListOfRelatedFiles(jsonFile);
		expect(compressionUtils.untarFiles(anyObject(File.class))).andReturn(uncompressedFiles);
		
		ExperimentJsonImporter jsonImporter = createMock(ExperimentJsonImporter.class);
		jsonImporter.loadModelsFromJson(jsonFile);
		expect(jsonImporter.getExperiment()).andReturn(experiment);
		expect(jsonImporter.getPonderations()).andReturn(ponderations);
		
		DatabaseFiles databaseFiles = createMock(DatabaseFiles.class);
		databaseFiles.store(experiment.alignments.get(0), alignmentFile);
		databaseFiles.store(experiment.targetMolecules, moleculeDatabaseFile);
		databaseFiles.store(experiment.targetMolecules.getAllDeployments().get(0), deploymentFile);
		databaseFiles.store(experiment.probeMolecules, moleculeDatabaseFile);
		databaseFiles.store(experiment.probeMolecules.getAllDeployments().get(0), deploymentFile);
		
		replay(compressionUtils, jsonImporter, databaseFiles);

		importer.setCompressionUtils(compressionUtils);
		importer.setDatabaseFiles(databaseFiles);
		importer.setJsonImporter(jsonImporter);
		
		File inputFile = new File("test-files/");
		JPA.setRollbackOnly();
		importer.importExperiment(inputFile);
		
		for(Alignment alignment: experiment.alignments){
			assertEquals(alignmentFile, alignment.transientFile);
		}
		verifyTransientFilesInMoleculeDatabase(experiment.targetMolecules);
		verifyTransientFilesInMoleculeDatabase(experiment.probeMolecules);
		
		verify(compressionUtils, jsonImporter, databaseFiles);
	}

	@Test
	public void importedExperimentIsSavedToDatabase(){
		ExperimentImporter importer = new ExperimentImporter();
		
		CompressionUtils compressionUtils = createMock(CompressionUtils.class);
		compressionUtils.uncompress(anyObject(File.class),	anyObject(File.class));
		File jsonFile = new File("test-files/json/experiment1mol1dep1propEvaluatedExport.json");
		List<File> uncompressedFiles = getListOfRelatedFiles(jsonFile);
		expect(compressionUtils.untarFiles(anyObject(File.class))).andReturn(uncompressedFiles);
		
		ExperimentJsonImporter jsonImporter = createMock(ExperimentJsonImporter.class);
		jsonImporter.loadModelsFromJson(jsonFile);
		expect(jsonImporter.getExperiment()).andReturn(experiment);
		expect(jsonImporter.getPonderations()).andReturn(ponderations);
		
		DatabaseFiles databaseFiles = createNiceMock(DatabaseFiles.class);
		
		replay(compressionUtils, jsonImporter, databaseFiles);

		importer.setCompressionUtils(compressionUtils);
		importer.setDatabaseFiles(databaseFiles);
		importer.setJsonImporter(jsonImporter);
		
		File inputFile = new File("test-files/");
		JPA.setRollbackOnly();
		importer.importExperiment(inputFile);

		assertTrue(experiment.isPersistent());
		for(Ponderation ponderation : ponderations){
			assertTrue(ponderation.isPersistent());
		}
		
		verify(compressionUtils, jsonImporter, databaseFiles);
	}
	
	@Test(expected = RuntimeException.class)
	public void failsWhenAlignmentFileIsNotFound(){
		ExperimentImporter importer = new ExperimentImporter();
		
		CompressionUtils compressionUtils = createMock(CompressionUtils.class);
		compressionUtils.uncompress(anyObject(File.class),	anyObject(File.class));
		File jsonFile = new File("test-files/json/experiment1mol1dep1propEvaluatedExport.json");
		List<File> uncompressedFiles = getListOfRelatedFiles(jsonFile);
		expect(compressionUtils.untarFiles(anyObject(File.class))).andReturn(uncompressedFiles);
		
		ExperimentJsonImporter jsonImporter = createMock(ExperimentJsonImporter.class);
		jsonImporter.loadModelsFromJson(jsonFile);
		expect(jsonImporter.getExperiment()).andReturn(experiment);
		expect(jsonImporter.getPonderations()).andReturn(ponderations);
		
		DatabaseFiles databaseFiles = createNiceMock(DatabaseFiles.class);
		
		replay(compressionUtils, jsonImporter, databaseFiles);

		importer.setCompressionUtils(compressionUtils);
		importer.setDatabaseFiles(databaseFiles);
		importer.setJsonImporter(jsonImporter);
		
		uncompressedFiles.remove(this.alignmentFile);
		
		File inputFile = new File("test-files/");
		JPA.setRollbackOnly();
		importer.importExperiment(inputFile);

		verify(compressionUtils, jsonImporter, databaseFiles);
	}

	@Test(expected = RuntimeException.class)
	public void failsWhenDeploymentFileIsNotFound(){
		ExperimentImporter importer = new ExperimentImporter();
		
		CompressionUtils compressionUtils = createMock(CompressionUtils.class);
		compressionUtils.uncompress(anyObject(File.class),	anyObject(File.class));
		File jsonFile = new File("test-files/json/experiment1mol1dep1propEvaluatedExport.json");
		List<File> uncompressedFiles = getListOfRelatedFiles(jsonFile);
		expect(compressionUtils.untarFiles(anyObject(File.class))).andReturn(uncompressedFiles);
		
		ExperimentJsonImporter jsonImporter = createMock(ExperimentJsonImporter.class);
		jsonImporter.loadModelsFromJson(jsonFile);
		expect(jsonImporter.getExperiment()).andReturn(experiment);
		expect(jsonImporter.getPonderations()).andReturn(ponderations);
		
		DatabaseFiles databaseFiles = createNiceMock(DatabaseFiles.class);
		
		replay(compressionUtils, jsonImporter, databaseFiles);

		importer.setCompressionUtils(compressionUtils);
		importer.setDatabaseFiles(databaseFiles);
		importer.setJsonImporter(jsonImporter);
		
		uncompressedFiles.remove(this.deploymentFile);
		uncompressedFiles.remove(this.deploymentFile);
		
		File inputFile = new File("test-files/");
		JPA.setRollbackOnly();
		importer.importExperiment(inputFile);

		verify(compressionUtils, jsonImporter, databaseFiles);
	}

	@Test(expected = RuntimeException.class)
	public void failsWhenMoleculeDatabaseFileIsNotFound(){
		ExperimentImporter importer = new ExperimentImporter();
		
		CompressionUtils compressionUtils = createMock(CompressionUtils.class);
		compressionUtils.uncompress(anyObject(File.class),	anyObject(File.class));
		File jsonFile = new File("test-files/json/experiment1mol1dep1propEvaluatedExport.json");
		List<File> uncompressedFiles = getListOfRelatedFiles(jsonFile);
		expect(compressionUtils.untarFiles(anyObject(File.class))).andReturn(uncompressedFiles);
		
		ExperimentJsonImporter jsonImporter = createMock(ExperimentJsonImporter.class);
		jsonImporter.loadModelsFromJson(jsonFile);
		expect(jsonImporter.getExperiment()).andReturn(experiment);
		expect(jsonImporter.getPonderations()).andReturn(ponderations);
		
		DatabaseFiles databaseFiles = createNiceMock(DatabaseFiles.class);
		
		replay(compressionUtils, jsonImporter, databaseFiles);

		importer.setCompressionUtils(compressionUtils);
		importer.setDatabaseFiles(databaseFiles);
		importer.setJsonImporter(jsonImporter);
		
		uncompressedFiles.remove(this.moleculeDatabaseFile);
		uncompressedFiles.remove(this.moleculeDatabaseFile);
		
		File inputFile = new File("test-files/");
		JPA.setRollbackOnly();
		importer.importExperiment(inputFile);

		verify(compressionUtils, jsonImporter, databaseFiles);
	}
}
