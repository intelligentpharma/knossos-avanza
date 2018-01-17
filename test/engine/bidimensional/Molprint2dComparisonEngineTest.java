package engine.bidimensional;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import models.ComparisonExperiment;
import models.MoleculeDatabase;

import org.junit.Test;

import play.test.UnitTest;
import utils.Factory;
import utils.bidimensional.Compound;
import utils.bidimensional.FingerprintFrequencyMatrix;
import de.zbit.jcmapper.fingerprinters.topological.Encoding2DMolprint;
import files.DatabaseFiles;

public class Molprint2dComparisonEngineTest extends UnitTest{

	@Test
	public void retrievesMoleculeDatabasesAndStoresOutputFile(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.targetMolecules = new MoleculeDatabase();
		experiment.probeMolecules = new MoleculeDatabase();
		experiment.targetMolecules.originalFileName = "targetFile.sdf";
		experiment.probeMolecules.originalFileName = "probeFile.sdf";
		
		Molprint2dComparisonEngine engine = new Molprint2dComparisonEngine();

		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		dbFiles.store(anyObject(ComparisonExperiment.class), anyObject(File.class));

		Factory factory = createMock(Factory.class);
		FingerprintFrequencyMatrix targetMatrix = createMock(FingerprintFrequencyMatrix.class);
		FingerprintFrequencyMatrix probeMatrix = createMock(FingerprintFrequencyMatrix.class);
		expect(factory.createFingerprintFrequencyMatrix()).andReturn(targetMatrix);
		expect(factory.createFingerprintFrequencyMatrix()).andReturn(probeMatrix);
		
		engine.setDatabaseFiles(dbFiles);
		engine.setExperiment(experiment);
		engine.setFactory(factory);

		List<Compound> probeCompounds = new ArrayList<Compound>();
		expect(probeMatrix.getCompounds()).andReturn(probeCompounds);

		Encoding2DMolprint encodingMolprint = createMock(Encoding2DMolprint.class);
		
		engine.setFingerprinter(encodingMolprint);

		replay(dbFiles, encodingMolprint, factory, probeMatrix);
		
		engine.calculate();
		
		verify(dbFiles, factory, encodingMolprint, probeMatrix);
	}
		
}
