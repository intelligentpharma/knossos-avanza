package engine.bidimensional;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junitx.framework.FileAssert;
import models.ComparisonExperiment;
import models.MoleculeDatabase;

import org.junit.Test;

import play.test.UnitTest;
import utils.bidimensional.Compound;
import utils.bidimensional.FingerprintFrequencyMatrix;
import utils.bidimensional.Smile;
import files.DatabaseFiles;
import files.formats.smiles.SmilesDataExtractor;

public class LingosComparisonEngineTest extends UnitTest {

	@Test
	public void retrievesMoleculeDatabasesAndStoresOutputFile(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.targetMolecules = new MoleculeDatabase();
		experiment.probeMolecules = new MoleculeDatabase();
		
		LingosComparisonEngine engine = new LingosComparisonEngine();

		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		dbFiles.store(anyObject(ComparisonExperiment.class), anyObject(File.class));
		
		engine.setDatabaseFiles(dbFiles);
		engine.setExperiment(experiment);

		List<Compound> smiles = new ArrayList<Compound>();
		FingerprintFrequencyMatrix matrix = createMock(FingerprintFrequencyMatrix.class);
		expect(matrix.getCompounds()).andReturn(smiles);
		
		SmilesDataExtractor smilesExtractor = createMock(SmilesDataExtractor.class);
		smilesExtractor.setMoleculeDatabase(anyObject(MoleculeDatabase.class));
		expectLastCall().times(2);
		smilesExtractor.extractSmilesData();
		expectLastCall().times(2);
		expect(smilesExtractor.getFingerprintFrequencyMatrix()).andReturn(matrix).times(2);
		
		engine.setSmilesExtractor(smilesExtractor);

		replay(dbFiles, smilesExtractor, matrix);
		
		engine.calculate();
		
		verify(dbFiles);
	}

	@Test
	public void outputFileIsCorrect(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.targetMolecules = new MoleculeDatabase();
		experiment.probeMolecules = new MoleculeDatabase();
		
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		dbFiles.store(anyObject(ComparisonExperiment.class), anyObject(File.class));
		
		List<Compound> targetSmiles = new ArrayList<Compound>();
		targetSmiles.add(new Smile("COCOCO","mol1"));
		FingerprintFrequencyMatrix targetMatrix = createMock(FingerprintFrequencyMatrix.class);
		expect(targetMatrix.getCompounds()).andReturn(targetSmiles).anyTimes();
		
		List<Compound> probeSmiles = new ArrayList<Compound>();
		probeSmiles.add(new Smile("COCOCO","mol2"));
		probeSmiles.add(new Smile("COCHCO","mol3"));
		FingerprintFrequencyMatrix probeMatrix = createMock(FingerprintFrequencyMatrix.class);
		expect(probeMatrix.getCompounds()).andReturn(probeSmiles).anyTimes();
		
		SmilesDataExtractor smilesExtractor = createMock(SmilesDataExtractor.class);
		smilesExtractor.setMoleculeDatabase(experiment.targetMolecules);
		smilesExtractor.extractSmilesData();
		expect(smilesExtractor.getFingerprintFrequencyMatrix()).andReturn(targetMatrix);
		smilesExtractor.setMoleculeDatabase(experiment.probeMolecules);
		smilesExtractor.extractSmilesData();
		expect(smilesExtractor.getFingerprintFrequencyMatrix()).andReturn(probeMatrix);
		
		replay(dbFiles, smilesExtractor, targetMatrix, probeMatrix);
		
		LingosComparisonEngine engine = new LingosComparisonEngine();
		engine.setDatabaseFiles(dbFiles);
		engine.setSmilesExtractor(smilesExtractor);
		engine.setExperiment(experiment);
		
		engine.calculate();
		FileAssert.assertEquals(new File("test-files/pegasusOutput.csv"), engine.outputFile);
	}
}
