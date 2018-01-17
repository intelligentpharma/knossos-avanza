package engine.tridimensional.docking;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.File;
import java.io.IOException;

import models.AlignmentBox;
import models.ComparisonExperiment;
import models.Deployment;
import models.PhysicalSimilarities;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import utils.Factory;
import utils.scripts.SlurmExternalScript;
import engine.AbstractEngine;
import engine.EngineMediatorTest;
import engine.factory.FileNameFactory;
import engine.tridimensional.aligner.AbstractAutodockAligner;
import engine.tridimensional.aligner.AlignmentBoxCalculator;
import engine.tridimensional.docking.DockingEngine;
import engine.tridimensional.maps.MapsGenerator;
import engine.tridimensional.preparator.DeploymentPreparator;
import files.DatabaseFiles;
import files.FileDataExtractor;
import files.FileFormatTranslator;

public class DockingEngineTest extends EngineMediatorTest {

	DockingEngine engine;

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		engine = new DockingEngine();
	}

	@Override
	protected AbstractEngine getEngine() {
		return engine;
	}
			
	@Test
	public void calculateWithMocks() throws IOException {
		
		ComparisonExperiment experiment = new ComparisonExperiment();
		Deployment probe = new Deployment();
		Deployment target = new Deployment();
		experiment.box = new AlignmentBox(1,2,3,4,5,6);

		PhysicalSimilarities similarity = new PhysicalSimilarities();
		similarity.probeDeployment = probe;
		similarity.targetDeployment = target;
		similarity.experiment = experiment;
		
		Double energy = new Double(10.0);
		Double entropy = new Double(12.0);
		Double energyMedian = new Double(13.0);
		Double energyBigC = new Double(14.0);
		Double energyBigCMedian = new Double(15.0);
		FileDataExtractor fileDataExtractor = EasyMock.createMock(FileDataExtractor.class);
		fileDataExtractor.parseFile("tmp/file.dlg");
		EasyMock.expect(fileDataExtractor.getEnergy()).andReturn(energy).anyTimes();
		EasyMock.expect(fileDataExtractor.getEntropy()).andReturn(entropy).anyTimes();
		EasyMock.expect(fileDataExtractor.getEnergyMedian()).andReturn(energyMedian).anyTimes();
		EasyMock.expect(fileDataExtractor.getEnergyBigC()).andReturn(energyBigC).anyTimes();
		EasyMock.expect(fileDataExtractor.getEnergyBigCMedian()).andReturn(energyBigCMedian).anyTimes();

		FileNameFactory fileNameFactory = EasyMock.createMock(FileNameFactory.class);
		fileNameFactory.setExperiment(experiment);
		fileNameFactory.setExperimentTarget(target);
		fileNameFactory.setExperimentProbe(probe);
		EasyMock.expect(fileNameFactory.getTargetPdbqt()).andReturn("tmp/target.pdbqt").anyTimes();
		EasyMock.expect(fileNameFactory.getProbePdbqt()).andReturn("tmp/probe.pdbqt").anyTimes();
		EasyMock.expect(fileNameFactory.getOutputPdbqt()).andReturn("tmp/output.pdbqt").anyTimes();
		EasyMock.expect(fileNameFactory.getDlg()).andReturn("tmp/file.dlg").anyTimes();
		
		DeploymentPreparator preparator = EasyMock.createMock(DeploymentPreparator.class);
		preparator.prepare(probe, experiment.chargeType);

		AlignmentBoxCalculator targetParser = EasyMock.createMock(AlignmentBoxCalculator.class);
		targetParser.setDefaultAlignmentBox(experiment.box);
		EasyMock.expect(targetParser.getAlignmentBox()).andReturn(experiment.box).anyTimes();
		
		AlignmentBoxCalculator probeParser = EasyMock.createMock(AlignmentBoxCalculator.class);
		probeParser.calculateAlignmentBox("tmp/probe.pdbqt", null);
		
		AbstractAutodockAligner aligner = EasyMock.createMock(AbstractAutodockAligner.class);
		aligner.alignDeployments(similarity, SlurmExternalScript.DEFAULT_PARTITION);
		String outputVina = "Reading input ... done.\n" +
				"Setting up the scoring function ... done.\n" +
				"Affinity: -0.40378 (kcal/mol)\n" +
				"Intermolecular contributions to the terms, before weighting:\n" +
				"    gauss 1     : 6.63231\n" +
				"    gauss 2     : 103.31803\n" +
				"    repulsion   : 0.37164\n" +
				"    hydrophobic : 0.06548\n" +
				"    Hydrogen    : 0.00216";
		EasyMock.expect(aligner.rescoreVina()).andReturn(outputVina).anyTimes();

		DatabaseFiles databaseFiles = EasyMock.createMock(DatabaseFiles.class);
		FileFormatTranslator translator = EasyMock.createMock(FileFormatTranslator.class);
		
		EasyMock.replay(fileNameFactory, preparator, targetParser, probeParser, aligner, fileDataExtractor);
		
		String probeMoleculePath = "tmp/output.pdbqt";
		File staticFile = new File(probeMoleculePath);
		staticFile.createNewFile();
	
		engine.setFileNameFactory(fileNameFactory);
		engine.setTargetBoxCalculator(targetParser);
		engine.setProbeBoxCalculator(probeParser);
		engine.setTargetPreparator(preparator);
		engine.setProbePreparator(preparator);
		engine.setDeploymentAligner(aligner);
		engine.setFileDataExtractor(fileDataExtractor);
		engine.setDatabaseFiles(databaseFiles);
		engine.setFileFormatTranslator(translator);
		
		engine.calculate(similarity);

		assertEquals(10, similarity.energy, 0.01);
		assertEquals(12, similarity.entropy, 0.01);
		assertEquals(-0.40378,similarity.vinaEnergy,0.01);
		assertEquals(13, similarity.energyMedian, 0.01);
		assertEquals(14, similarity.energyBigC, 0.01);
		assertEquals(15, similarity.energyBigCMedian, 0.01);
		
		double ePenalty = 1-similarity.entropy;
		assertEquals(ePenalty*similarity.energyBigC, similarity.energyBigCEPenalty, 0.01);
		assertEquals(ePenalty*similarity.energyBigCMedian, similarity.energyBigCMedianEPenalty, 0.01);
		assertEquals(ePenalty*similarity.energy, similarity.energyEPenalty, 0.01);
		assertEquals(ePenalty*similarity.energyMedian, similarity.energyMedianEPenalty, 0.01);
		assertEquals(ePenalty*similarity.vinaEnergy, similarity.vinaEnergyEPenalty, 0.01);
		
		EasyMock.verify(fileNameFactory, preparator, targetParser, probeParser, aligner, fileDataExtractor);
	}
	
	@Test
	public void setTargetMoleculeAndPrepare() {
		
		Deployment deployment = new Deployment();
		
		FileNameFactory fileNameFactory = EasyMock.createMock(FileNameFactory.class);
		fileNameFactory.setExperimentTarget(deployment);
		EasyMock.expect(fileNameFactory.getTargetPdbqt()).andReturn("target.pdbqt").anyTimes();

		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.chargeType = Factory.GASTEIGER;
		fileNameFactory.setExperiment(experiment);

		DeploymentPreparator converter = EasyMock.createMock(DeploymentPreparator.class);
		converter.prepare(deployment, Factory.GASTEIGER);
		
		AlignmentBoxCalculator parser = EasyMock.createMock(AlignmentBoxCalculator.class);
		parser.setDefaultAlignmentBox(experiment.box);
		EasyMock.expect(parser.getAlignmentBox()).andReturn(experiment.box).anyTimes();
		
		MapsGenerator generator = EasyMock.createMock(MapsGenerator.class);
		generator.generateMaps(experiment.box);
		
		EasyMock.replay(fileNameFactory, converter, parser, generator);
		
		engine.setFileNameFactory(fileNameFactory);
		engine.setTargetPreparator(converter);
		engine.setTargetBoxCalculator(parser);
		engine.setPreAlignmentTargetMapsGenerator(generator);
		
		engine.setExperiment(experiment);
		engine.setTargetDeployment(deployment);
		engine.prepareTargetDeployment();
		
		EasyMock.verify(fileNameFactory, converter, parser, generator);
		assertEquals(deployment, engine.target);
	}
	
	@Test
	public void toStringReturnsName(){
		engine.name = "hello";
		assertEquals(engine.name, engine.toString());
	}
	
	@Test
	public void createsPhysicalSimilarities(){
		assertTrue(engine.createSimilarityModel() instanceof PhysicalSimilarities);
	}
	
	@Test
	public void getsMapsPrefixFromFileNameFactory(){
		AbstractEngine engine = getEngine();
		MapsGenerator generator = createMock(MapsGenerator.class);
		expect(generator.getMapsPrefix()).andReturn("maps");
		engine.setPreAlignmentTargetMapsGenerator(generator);
		
		replay(generator);
		
		assertEquals("maps", engine.getTargetMapsPrefix());
	}
}
