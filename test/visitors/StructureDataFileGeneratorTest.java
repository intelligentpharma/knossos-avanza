package visitors;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Alignment;
import models.ComparisonExperiment;
import models.Deployment;
import models.MapsSimilarities;
import models.PhysicalSimilarities;
import models.Ponderation;
import models.Scoring;
import models.User;

import org.junit.BeforeClass;
import org.junit.Test;

import play.test.Fixtures;
import utils.experiment.TestDataCreator;
import files.DatabaseFiles;
import files.FileFormatTranslator;
import files.FileGenerator;
import files.FileUtils;

public class StructureDataFileGeneratorTest extends FailingMethodsVisitorTest {

	static ComparisonExperiment docking, inverse;
	static Ponderation ponderation;
	
	@BeforeClass
	public static void setup(){
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		User owner = User.findByUserName("aperreau");
		TestDataCreator creator = new TestDataCreator();
		docking = creator.getSmallEvaluatedDockingExperiment(owner);
		inverse = creator.getSmallEvaluatedExperiment(owner);
		ponderation = Ponderation.getDefaultPonderations().get(0);
	}
	
	@Override
	public KnossosVisitor getVisitor() {
		StructureDataFileGeneratorOrderedByPonderation structureDataFileGenerator = new StructureDataFileGeneratorOrderedByPonderation(null, null, null, false);
		structureDataFileGenerator.setPonderation(ponderation);
		return structureDataFileGenerator;
	}
	
	@Override
	public int[] getFailingTypes() {
		int[] failingTypes =  {MOLECULE_DATABASE, DEPLOYMENT, SCORING, QSAR_EXPERIMENT, QSAR_RESULT};
		return failingTypes;
	}
	
	@Test
	public void notUniqueVisitsAllAlignments(){
		FileGenerator fileGenerator = createMock(FileGenerator.class);
		
		FileFormatTranslator translator = createMock(FileFormatTranslator.class);
		FileUtils fileUtils = createMock(FileUtils.class);
		
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		expect(dbFiles.retrieve(anyObject(Alignment.class))).andReturn(new File("test-files/test2.sdf")).anyTimes();
		
		StructureDataFileGeneratorOrderedByPonderation generator = new StructureDataFileGeneratorOrderedByPonderation(fileGenerator, translator, fileUtils, false);

		generator.setPonderation(ponderation);
		generator.setDatabaseFiles(dbFiles);
		
		replay(fileGenerator, translator, fileUtils, dbFiles);
		
		generator.visit(docking);
		
		assertEquals(7, generator.alignments.size());
		verify(fileGenerator, translator, fileUtils, dbFiles);
	}
	
	@Test
	public void uniqueDockingVisitsBestAlignmentsPerEnergy(){
		FileGenerator fileGenerator = createMock(FileGenerator.class);
		
		FileFormatTranslator translator = createMock(FileFormatTranslator.class);
		FileUtils fileUtils = createMock(FileUtils.class);
		
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		expect(dbFiles.retrieve(anyObject(Alignment.class))).andReturn(new File("test-files/test2.sdf")).anyTimes();
		
		StructureDataFileGeneratorOrderedByPonderation generator = new StructureDataFileGeneratorOrderedByPonderation(fileGenerator, translator, fileUtils, true);
		
		generator.setPonderation(ponderation);
		generator.setDatabaseFiles(dbFiles);
		
		replay(fileGenerator, translator, fileUtils, dbFiles);
		
		generator.visit(docking);
		
		assertEquals(6, generator.alignments.size());
		verify(fileGenerator, translator, fileUtils, dbFiles);
	}
	
	@Test
	public void uniqueInverseDockingVisitsBestAlignmentsPerEnergy(){
		FileGenerator fileGenerator = createMock(FileGenerator.class);
		
		FileFormatTranslator translator = createMock(FileFormatTranslator.class);
		FileUtils fileUtils = createMock(FileUtils.class);
		
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		expect(dbFiles.retrieve(anyObject(Alignment.class))).andReturn(new File("test-files/test2.sdf")).anyTimes();
		
		StructureDataFileGeneratorOrderedByPonderation generator = new StructureDataFileGeneratorOrderedByPonderation(fileGenerator, translator, fileUtils, true);
		
		generator.setPonderation(ponderation);
		generator.setDatabaseFiles(dbFiles);
		
		replay(fileGenerator, translator, fileUtils, dbFiles);
		
		generator.visit(inverse);
		
		assertEquals(6, generator.alignments.size());
		verify(fileGenerator, translator, fileUtils, dbFiles);
	}
	
	@Test
	public void structureDataFileIsCreatedByJoiningAllAlignmentFilesAndConvertingToOutputFormat() throws IOException{
		FileGenerator fileGenerator = createMock(FileGenerator.class);
		Map<String, String> equivalence = new HashMap<String, String>();
		expect(fileGenerator.getPonderationEquivalenceMap()).andReturn(equivalence);
		
		FileFormatTranslator translator = createMock(FileFormatTranslator.class);
		translator.convertPdbqtToSdf(anyObject(String.class), anyObject(String.class));

		FileUtils fileUtils = createMock(FileUtils.class);
		fileUtils.joinFiles(anyObject(List.class), anyObject(File.class));
		fileUtils.replaceEquivalencesInFile("jointPdbqt.sdf", equivalence);
		expect(fileUtils.getFileNameWithoutExtension(anyObject(File.class))).andReturn("jointPdbqt");
		
		replay(fileGenerator, translator, fileUtils);

		StructureDataFileGeneratorOrderedByPonderation generator = new StructureDataFileGeneratorOrderedByPonderation(fileGenerator, translator, fileUtils, true);
		
		generator.setPonderation(ponderation);
		generator.createStructureDataFile();
		
		verify(fileGenerator, translator, fileUtils);
	}
	
	@Test
	public void visitingAlignmentWithoutFileDoesNothing(){
		PhysicalSimilarities alignment = new PhysicalSimilarities();
		//alignment.alignmentFile = new Blob();
		StructureDataFileGeneratorOrderedByPonderation generator = new StructureDataFileGeneratorOrderedByPonderation(null, null, null, true);
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		expect(dbFiles.retrieve(alignment)).andReturn(new File("nonExistentFile"));
		generator.setDatabaseFiles(dbFiles);
		
		replay(dbFiles);
		
		generator.visit(alignment);
		
		assertTrue(generator.alignments.isEmpty());
	}

	@Test
	public void visitingAlignmentWithoutScoresDoesNothing(){
		TestDataCreator creator = new TestDataCreator();
		ComparisonExperiment experiment = creator.getSmallEvaluatedExperiment(User.findByUserName("aperreau"));
		MapsSimilarities alignment = (MapsSimilarities)experiment.alignments.get(0);
		for(Scoring scoring : alignment.scorings){
			scoring.delete();
		}
		StructureDataFileGeneratorOrderedByPonderation generator = new StructureDataFileGeneratorOrderedByPonderation(null, null, null, true);
		generator.visit(alignment);
	}

	@Test
	public void shouldNeverVisitDeployment(){
		try {
			StructureDataFileGeneratorOrderedByPonderation generator = new StructureDataFileGeneratorOrderedByPonderation(null, null, null, true);
			generator.visit(new Deployment());
			fail("Should never visit a deployment");
		} catch (Exception e){
			
		}
	}
	
	@Test
	public void shouldNeverVisitScoring(){
		try {
			StructureDataFileGeneratorOrderedByPonderation generator = new StructureDataFileGeneratorOrderedByPonderation(null, null, null, true);
			generator.visit(new Scoring());
			fail("Should never visit a scoring");
		} catch (Exception e){
			
		}
	}

	@Test
	public void multipleExperimentsStructureDataFileIsCreatedCorrectly() throws IOException{
		TestDataCreator creator = new TestDataCreator();
		ComparisonExperiment experiment1 = creator.getSmallEvaluatedExperiment(User.findByUserName("aperreau"));
		double score = 1;
		for(Alignment alignment : experiment1.alignments){
			for(Scoring scoring : alignment.scorings){
				scoring.score = score++;
			}
		}
		ComparisonExperiment experiment2 = creator.getSmallEvaluatedExperiment(User.findByUserName("aperreau"));
		score = 0.5;
		for(Alignment alignment : experiment2.alignments){
			for(Scoring scoring : alignment.scorings){
				scoring.score = score++; 
			}
		}

		FileGenerator fileGenerator = createMock(FileGenerator.class);
		fileGenerator.parse(anyObject(Alignment.class));
		expectLastCall().anyTimes();
		expect(fileGenerator.getOutputFile()).andReturn(new File("test-files/test2.sdf")).anyTimes();
		Map<String, String> ponderationsMap = new HashMap<String, String>();
		expect(fileGenerator.getPonderationEquivalenceMap()).andReturn(ponderationsMap);
		
		FileUtils fileUtilsMock = createMock(FileUtils.class);
		fileUtilsMock.joinFiles(anyObject(List.class), anyObject(File.class));
		expect(fileUtilsMock.getFileNameWithoutExtension(anyObject(File.class))).andReturn("fileName").anyTimes();
		fileUtilsMock.replaceEquivalencesInFile("fileName.sdf", ponderationsMap);
		
		FileFormatTranslator translator = createMock(FileFormatTranslator.class);
		translator.convertPdbqtToSdf(anyObject(String.class), anyObject(String.class));
		
		StructureDataFileGeneratorOrderedByPonderation generator = new StructureDataFileGeneratorOrderedByPonderation(fileGenerator, translator, fileUtilsMock, false);
		
		generator.setPonderation(ponderation);
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		expect(dbFiles.retrieve(anyObject(Alignment.class))).andReturn(new File("test-files/test2.sdf")).anyTimes();
		generator.setDatabaseFiles(dbFiles);

		replay(fileGenerator, dbFiles, fileUtilsMock, translator);

		generator.visit(experiment1);
		generator.visit(experiment2);
		
		String outputFileName = generator.createStructureDataFile();
		
		verify(fileGenerator, dbFiles, fileUtilsMock, translator);
		
		assertEquals("fileName.sdf", outputFileName);
		for(int i=0; i< generator.alignments.size()-1; i++){
			assertTrue(generator.alignments.get(i).compare(generator.alignments.get(i+1), ponderation) > 0);
		}
	}
	
}
