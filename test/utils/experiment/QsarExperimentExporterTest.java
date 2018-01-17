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
import json.QsarExperimentJsonExporter;
import models.Deployment;
import models.MoleculeDatabase;
import models.QsarExperiment;

import org.apache.commons.compress.compressors.CompressorException;
import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.db.jpa.JPA;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.CompressionUtils;
import utils.ModelUtils;
import utils.experiment.QsarExperimentExporter;
import files.DatabaseFiles;
import files.FileUtils;

public class QsarExperimentExporterTest extends UnitTest{
	
	TestDataCreator dataCreator;
	QsarExperiment experiment;
	long fakeId1 = 1234;
	long fakeId2 = 5678;
	String storagePath;

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		JPA.setRollbackOnly();
		this.dataCreator = new TestDataCreator();
		this.experiment = dataCreator.create1mol1dep1propEvaluatedQsarExperiment();
		saveDatabasesAndExperiment(this.experiment);
		ModelUtils.setAllModelIdsToGivenId(this.experiment, fakeId1);
	}
	
	private void saveDatabasesAndExperiment(QsarExperiment experiment) {
		experiment.molecules.save();
		experiment.save();
	}

	@Test
	public void Onemol1dep1propDatabasesFilesListIsCorrect(){
		QsarExperimentExporter exporter = new QsarExperimentExporter();
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		exporter.setDatabaseFiles(dbFiles);
		expect(dbFiles.getFileName(anyObject(MoleculeDatabase.class))).andReturn("pathToDatabase").times(2);
		expect(dbFiles.getFileName(anyObject(Deployment.class))).andReturn("pathToDeployment").times(2);
		AbstractExperimentJsonExporter jsonExporter = createNiceMock(QsarExperimentJsonExporter.class);
		exporter.setJsonExporter(jsonExporter);
		FileUtils fileUtils = createNiceMock(FileUtils.class);
		exporter.setFileUtils(fileUtils);
		
		replay(dbFiles, jsonExporter, fileUtils);
		
		exporter.setExperiment(experiment);
		
		List<String> files = exporter.getMoleculeDatabasesFilesPath();

		assertEquals(2,files.size());

		assertEquals(files.get(0), "pathToDatabase");
		assertEquals(files.get(1), "pathToDeployment");
	}
	
	@Test
	public void OneMol1dep1propAllFilesListIsCorrect(){
		QsarExperimentExporter exporter = new QsarExperimentExporter();
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		exporter.setDatabaseFiles(dbFiles);
		expect(dbFiles.getFileName(anyObject(MoleculeDatabase.class))).andReturn("pathToDatabase").times(2);
		expect(dbFiles.getFileName(anyObject(Deployment.class))).andReturn("pathToDeployment").times(2);
		AbstractExperimentJsonExporter jsonExporter = createNiceMock(QsarExperimentJsonExporter.class);
		exporter.setJsonExporter(jsonExporter);
		exporter.setDatabaseFiles(dbFiles);
		expect(dbFiles.getFileName(anyObject(MoleculeDatabase.class))).andReturn("test-files/test2.sdf").times(1);
		expect(dbFiles.getFileName(anyObject(Deployment.class))).andReturn("test-files/test2.sdf").times(1);
		expect(dbFiles.getPath(anyObject(QsarExperiment.class))).andReturn("test-files/qsar/sampleExperiment/data/experiment").times(1);				
		FileUtils fileUtils = createNiceMock(FileUtils.class);
		exporter.setFileUtils(fileUtils);
		File[] fileList = getListOfRelatedFiles();
		expect(fileUtils.listFilesInADirectory("test-files/qsar/sampleExperiment/data/experiment")).andReturn(fileList);

		replay(dbFiles, jsonExporter, fileUtils);
		
		exporter.setExperiment(experiment);
		
		List<String> allFiles = exporter.getAllFilesPath();
		Logger.info(allFiles.size()+"");

		assertEquals(19,allFiles.size());
	}
	
	private File[] getListOfRelatedFiles() {
		File folder = new File("test-files/qsar/sampleExperiment/data/experiment");		
		File[] listOfFiles = folder.listFiles(); 
		return listOfFiles;
	}

	@Test
	public void notEvaluatedExperimentZipFileIsCorrect() throws IOException, CompressorException{
		QsarExperimentExporter exporter = new QsarExperimentExporter();
		AbstractExperimentJsonExporter jsonExporter = createNiceMock(QsarExperimentJsonExporter.class);
		jsonExporter.setExperiment(experiment);
		exporter.setJsonExporter(jsonExporter);
		CompressionUtils compressionUtils = createMock(CompressionUtils.class);
		exporter.setJsonExporter(jsonExporter);
		exporter.setCompressionUtils(compressionUtils);
		compressionUtils.tarGzFiles(anyObject(List.class), anyObject(File.class));
		expect(jsonExporter.getExperimentJsonForExport()).andReturn(new File("test-files/json/short.json"));
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		exporter.setDatabaseFiles(dbFiles);
		expect(dbFiles.getFileName(anyObject(MoleculeDatabase.class))).andReturn("test-files/test2.sdf").times(1);
		expect(dbFiles.getFileName(anyObject(Deployment.class))).andReturn("test-files/test2.sdf").times(1);
		expect(dbFiles.getPath(anyObject(QsarExperiment.class))).andReturn("test-files/qsar/sampleExperiment/data/experiment").times(1);		
		FileUtils fileUtils = createNiceMock(FileUtils.class);
		exporter.setFileUtils(fileUtils);
		File[] emptyFileList = new File[0];
		expect(fileUtils.listFilesInADirectory("test-files/qsar/sampleExperiment/data/experiment")).andReturn(emptyFileList);

		replay(jsonExporter, dbFiles, compressionUtils, fileUtils);
		
		exporter.setExperiment(experiment);
		
		exporter.getCompressedExperimentFile();

		verify(jsonExporter, dbFiles, compressionUtils, fileUtils);
	}


}
