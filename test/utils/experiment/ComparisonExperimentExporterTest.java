package utils.experiment;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.IOException;
import java.util.List;

import json.AbstractExperimentJsonExporter;
import json.ComparisonExperimentJsonExporter;
import json.ExperimentJsonExporter;
import models.Alignment;
import models.ComparisonExperiment;
import models.Deployment;
import models.MoleculeDatabase;

import org.apache.commons.compress.compressors.CompressorException;
import org.junit.Before;
import org.junit.Test;

import play.db.jpa.JPA;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.CompressionUtils;
import utils.ModelUtils;
import utils.TemplatedConfiguration;
import utils.experiment.ComparisonExperimentExporter;
import files.DatabaseFiles;


public class ComparisonExperimentExporterTest extends UnitTest {

	TestDataCreator dataCreator;
	ComparisonExperiment simpleExperiment;
	ComparisonExperiment experiment1mol1dep1prop;
	long fakeId1 = 1234;
	long fakeId2 = 5678;
	String storagePath;
	
	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		JPA.setRollbackOnly();
		this.dataCreator = new TestDataCreator();
		this.simpleExperiment = dataCreator.createExperiment();
		this.experiment1mol1dep1prop = dataCreator.create1mol1dep1propEvaluatedExperiment();
		saveDatabasesAndExperiment(this.simpleExperiment);
		saveDatabasesAndExperiment(this.experiment1mol1dep1prop);
		ModelUtils.setAllModelIdsToGivenId(this.simpleExperiment, fakeId1);
		ModelUtils.setAllModelIdsToGivenId(this.experiment1mol1dep1prop, fakeId2);
		storagePath = TemplatedConfiguration.get("database.files.storage.path");
	}

	private void saveDatabasesAndExperiment(ComparisonExperiment experiment) {
		experiment.targetMolecules.save();
		experiment.probeMolecules.save();
		experiment.save();
	}
	
	@Test
	public void notEvaluatedExperimentAligmentFilesListIsEmpty(){
		ComparisonExperimentExporter exporter = new ComparisonExperimentExporter();
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		exporter.setDatabaseFiles(dbFiles);
		AbstractExperimentJsonExporter jsonExporter = createNiceMock(ComparisonExperimentJsonExporter.class);
		exporter.setJsonExporter(jsonExporter);
		replay(dbFiles, jsonExporter);

		exporter.setExperiment(simpleExperiment);
		
		List<String> alignmentsFiles = exporter.getAlignmentFilesPath();

		assertEquals(0,alignmentsFiles.size());
	}

	@Test
	public void OneMol1dep1propAligmentFilesListIsCorrect(){
		ComparisonExperimentExporter exporter = new ComparisonExperimentExporter();
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		exporter.setDatabaseFiles(dbFiles);
		expect(dbFiles.getFileName(experiment1mol1dep1prop.alignments.get(0))).andReturn("pathToAlignment");
		AbstractExperimentJsonExporter jsonExporter = createNiceMock(ComparisonExperimentJsonExporter.class);
		exporter.setJsonExporter(jsonExporter);
		replay(dbFiles,jsonExporter);
		
		exporter.setExperiment(experiment1mol1dep1prop);

		List<String> alignmentsFiles = exporter.getAlignmentFilesPath();

		assertEquals(1,alignmentsFiles.size());
		
		assertEquals(alignmentsFiles.get(0), "pathToAlignment");
	}

	@Test
	public void Onemol1dep1propDatabasesFilesListIsCorrect(){
		ComparisonExperimentExporter exporter = new ComparisonExperimentExporter();
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		exporter.setDatabaseFiles(dbFiles);
		expect(dbFiles.getFileName(anyObject(MoleculeDatabase.class))).andReturn("pathToDatabase").times(2);
		expect(dbFiles.getFileName(anyObject(Deployment.class))).andReturn("pathToDeployment").times(2);
		AbstractExperimentJsonExporter jsonExporter = createNiceMock(ComparisonExperimentJsonExporter.class);
		exporter.setJsonExporter(jsonExporter);
		replay(dbFiles, jsonExporter);
		
		exporter.setExperiment(experiment1mol1dep1prop);
		
		List<String> alignmentsFiles = exporter.getMoleculeDatabasesFilesPath();

		assertEquals(4,alignmentsFiles.size());

		assertEquals(alignmentsFiles.get(0), "pathToDatabase");
		assertEquals(alignmentsFiles.get(1), "pathToDeployment");
		assertEquals(alignmentsFiles.get(2), "pathToDatabase");
		assertEquals(alignmentsFiles.get(3), "pathToDeployment");
	}
	

	@Test
	public void OneMol1dep1propAllFilesListIsCorrect(){
		ComparisonExperimentExporter exporter = new ComparisonExperimentExporter();
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		exporter.setDatabaseFiles(dbFiles);
		expect(dbFiles.getFileName(anyObject(Alignment.class))).andReturn("pathToAlignment");
		expect(dbFiles.getFileName(anyObject(MoleculeDatabase.class))).andReturn("pathToDatabase").times(2);
		expect(dbFiles.getFileName(anyObject(Deployment.class))).andReturn("pathToDeployment").times(2);
		AbstractExperimentJsonExporter jsonExporter = createNiceMock(ComparisonExperimentJsonExporter.class);
		exporter.setJsonExporter(jsonExporter);
		replay(dbFiles, jsonExporter);
		
		exporter.setExperiment(experiment1mol1dep1prop);
		
		List<String> allFiles = exporter.getAllFilesPath();

		assertEquals(5,allFiles.size());
	}
	
	@Test
	public void notEvaluatedExperimentZipFileIsCorrect() throws IOException, CompressorException{
		ComparisonExperimentExporter exporter = new ComparisonExperimentExporter();
		AbstractExperimentJsonExporter jsonExporter = createNiceMock(ComparisonExperimentJsonExporter.class);
		jsonExporter.setExperiment(experiment1mol1dep1prop);
		exporter.setJsonExporter(jsonExporter);
		CompressionUtils compressionUtils = createMock(CompressionUtils.class);
		exporter.setJsonExporter(jsonExporter);
		exporter.setCompressionUtils(compressionUtils);
		compressionUtils.tarGzFiles(anyObject(List.class), anyObject(File.class));
		expect(jsonExporter.getExperimentJsonForExport())
			.andReturn(new File("test-files/json/short.json"));
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		exporter.setDatabaseFiles(dbFiles);
		expect(dbFiles.getFileName(anyObject(Alignment.class))).andReturn("test-files/test2.sdf");
		expect(dbFiles.getFileName(anyObject(MoleculeDatabase.class))).andReturn("test-files/test2.sdf").times(2);
		expect(dbFiles.getFileName(anyObject(Deployment.class))).andReturn("test-files/test2.sdf").times(2);
		replay(jsonExporter, dbFiles, compressionUtils);
		
		exporter.setExperiment(experiment1mol1dep1prop);
		
		exporter.getCompressedExperimentFile();

		verify(jsonExporter, dbFiles, compressionUtils);
	}

	
}
