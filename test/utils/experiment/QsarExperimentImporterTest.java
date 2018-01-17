package utils.experiment;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import json.ExperimentJsonImporter;
import models.Deployment;
import models.MoleculeDatabase;
import models.Ponderation;
import models.QsarExperiment;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.db.jpa.JPA;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.CompressionUtils;
import utils.ModelUtils;
import utils.experiment.QsarExperimentImporter;
import files.DatabaseFiles;
import files.FileUtils;

public class QsarExperimentImporterTest extends UnitTest{

	QsarExperiment experiment;
	File moleculeDatabaseFile;
	File deploymentFile;

	@Before
	public void setup(){
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		TestDataCreator dataCreator = new TestDataCreator();
		experiment = dataCreator.create1mol1dep1propEvaluatedQsarExperiment();
		JPA.em().detach(experiment);
		ModelUtils.setAllModelIdsToGivenId(experiment, 666L);
	}

	@Test
	public void setsOwnerToJsonImporter(){
		User owner = new User(null,null,null);
		QsarExperimentImporter importer = new QsarExperimentImporter();
		ExperimentJsonImporter jsonImporter = createMock(ExperimentJsonImporter.class);
		jsonImporter.setOwner(owner);
		importer.setJsonImporter(jsonImporter);
		replay(jsonImporter);
		
		importer.setOwner(owner);
		
		verify(jsonImporter);
	}
	
	@Test
	public void experimentIsLoadedFromJsonFileInGzip(){
		QsarExperimentImporter importer = new QsarExperimentImporter();
		
		CompressionUtils compressionUtils = createMock(CompressionUtils.class);
		compressionUtils.uncompress(anyObject(File.class),	anyObject(File.class));
		File jsonFile = new File("test-files/json/qsarExperimentImport.json");
		List<File> uncompressedFiles = getListOfRelatedFiles(jsonFile);
		expect(compressionUtils.untarFiles(anyObject(File.class))).andReturn(uncompressedFiles);
		
		ExperimentJsonImporter jsonImporter = createMock(ExperimentJsonImporter.class);
		jsonImporter.loadModelsFromJson(jsonFile);
		expect(jsonImporter.getQsarExperiment()).andReturn(experiment);
		
		DatabaseFiles databaseFiles = createNiceMock(DatabaseFiles.class);
		FileUtils fileUtils = createNiceMock(FileUtils.class);
		
		replay(compressionUtils, jsonImporter, databaseFiles, fileUtils);

		importer.setCompressionUtils(compressionUtils);
		importer.setDatabaseFiles(databaseFiles);
		importer.setJsonImporter(jsonImporter);
		importer.setFileUtils(fileUtils);
		
		File inputFile = new File("test-files/");
		JPA.setRollbackOnly();
		importer.importExperiment(inputFile);
		
		verify(compressionUtils, jsonImporter, databaseFiles, fileUtils);
	}
	
	private List<File> getListOfRelatedFiles(File jsonFile) {
		List<File> uncompressedFiles = new ArrayList<File>();
		moleculeDatabaseFile = new File("db/666/database");
		deploymentFile = new File("db/666/66/deployment_666");
		uncompressedFiles.add(jsonFile);
		uncompressedFiles.add(moleculeDatabaseFile);
		uncompressedFiles.add(deploymentFile);
		uncompressedFiles.add(moleculeDatabaseFile);
		uncompressedFiles.add(deploymentFile);
		File folder = new File("test-files/qsar/sampleExperiment/data/experiment");		
		File[] listOfFiles = folder.listFiles(); 
		for(int i=0;i<listOfFiles.length;i++){
			uncompressedFiles.add(listOfFiles[i]);
		}
		return uncompressedFiles;
	}
	
	@Test
	public void importedExperimentHasLinksToAllTransientFiles(){
		QsarExperimentImporter importer = new QsarExperimentImporter();
		
		CompressionUtils compressionUtils = createMock(CompressionUtils.class);
		compressionUtils.uncompress(anyObject(File.class),	anyObject(File.class));
		File jsonFile = new File("test-files/json/qsarExperimentImport.json");
		List<File> uncompressedFiles = getListOfRelatedFiles(jsonFile);
		expect(compressionUtils.untarFiles(anyObject(File.class))).andReturn(uncompressedFiles);
		
		ExperimentJsonImporter jsonImporter = createMock(ExperimentJsonImporter.class);
		jsonImporter.loadModelsFromJson(jsonFile);
		expect(jsonImporter.getQsarExperiment()).andReturn(experiment);
		
		DatabaseFiles databaseFiles = createNiceMock(DatabaseFiles.class);
		FileUtils fileUtils = createNiceMock(FileUtils.class);
		
		replay(compressionUtils, jsonImporter, databaseFiles, fileUtils);

		importer.setCompressionUtils(compressionUtils);
		importer.setDatabaseFiles(databaseFiles);
		importer.setJsonImporter(jsonImporter);
		importer.setFileUtils(fileUtils);
		
		File inputFile = new File("test-files/");
		JPA.setRollbackOnly();
		importer.importExperiment(inputFile);
		
		verifyTransientFilesInMoleculeDatabase(experiment.molecules);
		
		verify(compressionUtils, jsonImporter, databaseFiles, fileUtils);
	}

	private void verifyTransientFilesInMoleculeDatabase(MoleculeDatabase molecules) {
		assertEquals(moleculeDatabaseFile, molecules.transientFile);
		for(Deployment deployment : molecules.getAllDeployments()){
			assertEquals(deploymentFile, deployment.transientFile);
		}
	}

	@Test
	public void filesAreStoredToTheRightPlace(){
		QsarExperimentImporter importer = new QsarExperimentImporter();
		
		CompressionUtils compressionUtils = createMock(CompressionUtils.class);
		compressionUtils.uncompress(anyObject(File.class),	anyObject(File.class));
		File jsonFile = new File("test-files/json/qsarExperimentImport.json");
		List<File> uncompressedFiles = getListOfRelatedFiles(jsonFile);
		expect(compressionUtils.untarFiles(anyObject(File.class))).andReturn(uncompressedFiles);
		
		ExperimentJsonImporter jsonImporter = createMock(ExperimentJsonImporter.class);
		jsonImporter.loadModelsFromJson(jsonFile);
		expect(jsonImporter.getQsarExperiment()).andReturn(experiment);
		
		DatabaseFiles databaseFiles = createMock(DatabaseFiles.class);
		databaseFiles.store(experiment.molecules, moleculeDatabaseFile);
		databaseFiles.store(experiment.molecules.getAllDeployments().get(0), deploymentFile);
		expect(databaseFiles.getPath(experiment)).andReturn("test-files/qsar/tmp");	
		FileUtils fileUtils = createNiceMock(FileUtils.class);
		fileUtils.createDirectory("test-files/qsar/tmp");
		File folder = new File("test-files/qsar/sampleExperiment/data/experiment");
		File[] files = folder.listFiles();
		for(int i=0;i<files.length;i++){
			fileUtils.copyFile(files[i].getPath(), "test-files/qsar/tmp/"+files[i].getName());
		}
		
		replay(compressionUtils, jsonImporter, databaseFiles, fileUtils);

		importer.setCompressionUtils(compressionUtils);
		importer.setDatabaseFiles(databaseFiles);
		importer.setJsonImporter(jsonImporter);
		importer.setFileUtils(fileUtils);
		
		File inputFile = new File("test-files/");
		JPA.setRollbackOnly();
		importer.importExperiment(inputFile);
		
		verifyTransientFilesInMoleculeDatabase(experiment.molecules);
		
		verify(compressionUtils, jsonImporter, databaseFiles, fileUtils);
				
	}

	@Test
	public void importedExperimentIsSavedToDatabase(){
		QsarExperimentImporter importer = new QsarExperimentImporter();
		
		CompressionUtils compressionUtils = createMock(CompressionUtils.class);
		compressionUtils.uncompress(anyObject(File.class),	anyObject(File.class));
		File jsonFile = new File("test-files/json/qsarExperimentImport.json");
		List<File> uncompressedFiles = getListOfRelatedFiles(jsonFile);
		expect(compressionUtils.untarFiles(anyObject(File.class))).andReturn(uncompressedFiles);
		
		ExperimentJsonImporter jsonImporter = createMock(ExperimentJsonImporter.class);
		jsonImporter.loadModelsFromJson(jsonFile);
		expect(jsonImporter.getQsarExperiment()).andReturn(experiment);
		
		DatabaseFiles databaseFiles = createMock(DatabaseFiles.class);
		databaseFiles.store(experiment.molecules, moleculeDatabaseFile);
		databaseFiles.store(experiment.molecules.getAllDeployments().get(0), deploymentFile);
		expect(databaseFiles.getPath(experiment)).andReturn("test-files/qsar/tmp");	
		FileUtils fileUtils = createNiceMock(FileUtils.class);
		fileUtils.createDirectory("test-files/qsar/tmp");
		File folder = new File("test-files/qsar/sampleExperiment/data/experiment");
		File[] files = folder.listFiles();
		for(int i=0;i<files.length;i++){
			fileUtils.copyFile(files[i].getPath(), "test-files/qsar/tmp/"+files[i].getName());
		}
		
		replay(compressionUtils, jsonImporter, databaseFiles, fileUtils);

		importer.setCompressionUtils(compressionUtils);
		importer.setDatabaseFiles(databaseFiles);
		importer.setJsonImporter(jsonImporter);
		importer.setFileUtils(fileUtils);
		
		File inputFile = new File("test-files/");
		JPA.setRollbackOnly();
		importer.importExperiment(inputFile);

		assertTrue(experiment.isPersistent());
		
		verify(compressionUtils, jsonImporter, databaseFiles, fileUtils);
	}



}
