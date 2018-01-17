package engine.tridimensional.docking;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.IOException;

import javax.persistence.EntityTransaction;

import models.Alignment;
import models.AlignmentBox;
import models.ComparisonExperiment;
import models.Deployment;
import models.MapsSimilarities;

import org.junit.Before;
import org.junit.Test;

import play.db.jpa.JPA;
import play.test.Fixtures;
import utils.Factory;
import utils.scripts.SlurmExternalScript;
import engine.AbstractEngine;
import engine.EngineMediatorTest;
import engine.factory.FileNameFactory;
import engine.tridimensional.aligner.AlignmentBoxCalculator;
import engine.tridimensional.aligner.DeploymentAligner;
import engine.tridimensional.docking.InverseDockingEngine;
import engine.tridimensional.maps.MapsGenerator;
import engine.tridimensional.maps.SimilarityCalculator;
import engine.tridimensional.preparator.DeploymentPreparator;
import files.FileUtils;

public class InverseDockingEngineTest extends EngineMediatorTest {

	InverseDockingEngine engine;

	@Before
	public void setup() {
		//This is needed cause failsIfThereAreTooMuchSmilesToConvert commits a transaction prior launching a RuntimeException
		EntityTransaction transaction = JPA.em().getTransaction();
		if(!transaction.isActive()){
			transaction.begin();
		}
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		engine = new InverseDockingEngine();
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
		Alignment similarity = new MapsSimilarities();
		similarity.probeDeployment = probe;
		similarity.targetDeployment = target;
		similarity.experiment = experiment;
		
		FileNameFactory fileNameFactory = createMock(FileNameFactory.class);
		fileNameFactory.setExperiment(experiment);
		fileNameFactory.setExperimentTarget(target);
		fileNameFactory.setExperimentProbe(probe);
		expect(fileNameFactory.getTargetPdbqt()).andReturn("tmp/target.pdbqt").anyTimes();
		expect(fileNameFactory.getProbePdbqt()).andReturn("tmp/probe.pdbqt").anyTimes();
		expect(fileNameFactory.getOutputPdbqt()).andReturn("tmp/output.pdbqt").anyTimes();
		
		FileUtils fileUtils = createMock(FileUtils.class);
		fileUtils.eraseFilesInDirectoryMatchingPattern("some", "maps.*\\.map");
		
		DeploymentPreparator preparator = createMock(DeploymentPreparator.class);
		preparator.prepare(probe, experiment.chargeType);

		AlignmentBox targetBox = new AlignmentBox(1,2,3,4,5,6);
		AlignmentBoxCalculator targetParser = createMock(AlignmentBoxCalculator.class);
		targetParser.calculateAlignmentBox("tmp/target.pdbqt", null);
		expect(targetParser.getAlignmentBox()).andReturn(targetBox).anyTimes();
		
		AlignmentBoxCalculator probeParser = createMock(AlignmentBoxCalculator.class);
		probeParser.calculateAlignmentBox("tmp/probe.pdbqt", null);
		
		DeploymentAligner aligner = createMock(DeploymentAligner.class);
		aligner.alignDeployments(similarity, SlurmExternalScript.DEFAULT_PARTITION);
		
		MapsGenerator mapsGenerator = createMock(MapsGenerator.class);
		mapsGenerator.generateMaps(targetBox);
		expect(mapsGenerator.getMapsPrefix()).andReturn("some/maps").anyTimes();
		
		SimilarityCalculator similarityCalculator = createMock(SimilarityCalculator.class);
		similarityCalculator.setBaseFileNames("some/maps", "some/maps");
		similarityCalculator.setSimilarityCalculationType(experiment.similarityCalculationType);
		similarityCalculator.calculateTanimotos();
		expect(similarityCalculator.getCalculatedTanimoto((String)anyObject())).andReturn(0.1).times(22);
		
		replay(fileNameFactory, preparator, targetParser, probeParser, 
				fileUtils, aligner, mapsGenerator, similarityCalculator);
		
		String probeMoleculePath = "tmp/output.pdbqt";
		File staticFile = new File(probeMoleculePath);
		staticFile.createNewFile();
	
		engine.setFileNameFactory(fileNameFactory);
		engine.setFileUtils(fileUtils);
		engine.setTargetBoxCalculator(targetParser);
		engine.setProbeBoxCalculator(probeParser);
		engine.setTargetPreparator(preparator);
		engine.setProbePreparator(preparator);
		engine.setDeploymentAligner(aligner);		
		engine.setSimilarityCalculator(similarityCalculator);		
		engine.setPreAlignmentTargetMapsGenerator(mapsGenerator);
		engine.setPostAlignmentTargetMapsGenerator(mapsGenerator);
		engine.setOutputMapsGenerator(mapsGenerator);
		
		engine.calculate(similarity);

		verify(fileNameFactory, preparator, targetParser, probeParser, 
				fileUtils, aligner, mapsGenerator, similarityCalculator);
	}
	
	@Test
	public void setTargetMoleculeAndPrepare() {
		
		Deployment deployment = new Deployment();
		
		FileNameFactory fileNameFactory = createMock(FileNameFactory.class);
		fileNameFactory.setExperimentTarget(deployment);
		expect(fileNameFactory.getTargetPdbqt()).andReturn("target.pdbqt").anyTimes();

		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.chargeType = Factory.GASTEIGER;
		fileNameFactory.setExperiment(experiment);

		DeploymentPreparator converter = createNiceMock(DeploymentPreparator.class);
		converter.prepare(deployment, Factory.GASTEIGER);
		expectLastCall();
		
		AlignmentBox deploymentInfo = new AlignmentBox();
		AlignmentBoxCalculator calculator = createMock(AlignmentBoxCalculator.class);
		calculator.calculateAlignmentBox("target.pdbqt", null);
		expect(calculator.getAlignmentBox()).andReturn(deploymentInfo).anyTimes();
		
		MapsGenerator generator = createNiceMock(MapsGenerator.class);
		generator.generateMaps(deploymentInfo);
		expectLastCall().times(2);
		
		replay(fileNameFactory, converter, calculator, generator);
		
		engine.setFileNameFactory(fileNameFactory);
		engine.setTargetPreparator(converter);
		engine.setTargetBoxCalculator(calculator);
		engine.setPreAlignmentTargetMapsGenerator(generator);
		engine.setPostAlignmentTargetMapsGenerator(generator);
		engine.setTargetDeployment(deployment);

		engine.setExperiment(experiment);

		engine.prepareTargetDeployment();
		
		verify(fileNameFactory, converter, calculator, generator);
		assertEquals(deployment, engine.target);
	}

	@Test
	public void failsIfThereAreTooMuchSmilesToConvert(){
		int TOO_MUCH_SMILES_TO_CONVERT = 2;
		ComparisonExperiment experiment = (ComparisonExperiment)ComparisonExperiment.find("byName", "Primera prueba").fetch().get(0);

		FileNameFactory fileNameFactory = createMock(FileNameFactory.class);
		Factory toolsFactory = createMock(Factory.class);
		expect(toolsFactory.getMaxSmilesToSdf()).andReturn(TOO_MUCH_SMILES_TO_CONVERT);

		experiment.targetMolecules.originalFileName = "foo.smi";
		experiment.targetMolecules.numMolecules = TOO_MUCH_SMILES_TO_CONVERT + 1;
		
		engine.setFileNameFactory(fileNameFactory);
		engine.setFactory(toolsFactory);
		engine.setExperiment(experiment);

		replay(toolsFactory);
		
		try{
			engine.convertDatabases();
			fail();
		}
		catch(Exception e){
			assertEquals("Cannot convert database Salts to 3d format cause it has more than 10000 molecules",e.getMessage());
			System.out.println(e.getMessage());
		}
		
		verify(toolsFactory);
	}
	
	
}
