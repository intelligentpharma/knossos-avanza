package engine.tridimensional.aligner;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import junitx.framework.FileAssert;
import models.Alignment;
import models.AlignmentBox;
import models.ComparisonExperiment;
import models.Deployment;
import models.MapsSimilarities;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;
import utils.TemplatedConfiguration;
import utils.scripts.ExternalScript;
import utils.scripts.SlurmExternalScript;
import utils.scripts.SlurmExternalScriptTest;
import engine.EngineMediator;
import engine.tridimensional.aligner.Autodock42Aligner;
import engine.tridimensional.maps.MapsNameExtractor;
import files.DatabaseFiles;

public class Autodock42AlignerTest extends UnitTest {

	Autodock42Aligner aligner;
	Deployment probe;
	Deployment target;
	ComparisonExperiment experiment;
	EngineMediator engine;

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");

		probe = new Deployment();
		target = new Deployment();
		experiment = new ComparisonExperiment();
		experiment.numRuns = 100;
		experiment.energyEvaluations = 2500000;
		experiment.chargeType = Factory.GASTEIGER;
		experiment.seed = "pid time";
		experiment.tran0 = "random";
		experiment.dihe0 = "random";
		experiment.tstep = "2.0";
		experiment.qstep = "50.0";
		experiment.dstep = "50.0";
		experiment.torsdof = "5";
		experiment.rmstol = "2.0";
		experiment.quat0 = "random";
		experiment.extnrg = "1000.0";
		experiment.e0max = "0.0 10000";
		experiment.ga_pop_size = "150";
		experiment.ga_num_generations = "27000";
		experiment.analysis = true;
		experiment.algorithm = "ga_run";
		
		AlignmentBox deploymentInfo = new AlignmentBox(1, 2, 3, 4, 5, 6);
		engine = createMock(EngineMediator.class);
		expect(engine.getProbeBox()).andReturn(deploymentInfo).anyTimes();
		expect(engine.getProbePdbqt()).andReturn("tmp/probe.pdbqt").anyTimes();
		expect(engine.getTargetMapsPrefix()).andReturn("tmp/target.maps").anyTimes();
		expect(engine.getDpf()).andReturn("tmp/AD4_2file.dpf").anyTimes();
		expect(engine.getDlg()).andReturn("tmp/file.dlg").anyTimes();
		expect(engine.getOutputPdbqt()).andReturn("tmp/file_out.pdbqt").anyTimes();

		aligner = new Autodock42Aligner();
	}

	@Test
	public void calculateWithMocks() throws IOException {

		ExternalScript launcher = createMock(ExternalScript.class);		
		expect(launcher.launch(String.format(TemplatedConfiguration.get("compress_maps"), "tmp/AD4_2file.dpf"))).andReturn("ok")
		.once();
		String autodock42 = TemplatedConfiguration.get("AD42");
		expect(launcher.paralelize("AD42", SlurmExternalScript.DEFAULT_PARTITION, autodock42 + " -p tmp/AD4_2file.dpf -l tmp/file.dlg")).andReturn("ok")
				.once();
		//This is expected only if there is no torsdof information provided in the experiment
		//expect(launcher.launch("grep TORSDOF tmp/probe.pdbqt")).andReturn("TORSDOF 5");

		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		dbFiles.store(anyObject(Alignment.class), anyObject(File.class));
		expectLastCall().anyTimes();
		expect(dbFiles.getFileName(anyObject(Alignment.class))).andReturn("alignmentFile").anyTimes();
		
		engine.extractDlgFromPdbqt();

		MapsNameExtractor mapsNameExtractor = createMock(MapsNameExtractor.class);
		Set<String> mapsNames = new TreeSet<String>();
		mapsNames.add("A");
		mapsNames.add("Br");
		mapsNames.add("F");
		mapsNames.add("Fe");
		mapsNames.add("HD");
		mapsNames.add("I");
		mapsNames.add("Mg");
		mapsNames.add("Mn");
		mapsNames.add("N");
		expect(mapsNameExtractor.getMapsNamesFromPdbqtFile(anyObject(File.class))).andReturn(mapsNames);
		
		replay(engine, launcher, dbFiles, mapsNameExtractor);

		aligner.setEngine(engine);
		aligner.setLauncher(launcher);
		aligner.setDatabaseFiles(dbFiles);
		aligner.setExperiment(experiment);
		aligner.mapsNameExtractor = mapsNameExtractor;
		aligner.ga_num_evals = experiment.energyEvaluations;
		aligner.ga_runs = experiment.numRuns;
		
		Alignment similarity = new MapsSimilarities();
		similarity.probeDeployment = probe;
		similarity.targetDeployment = target;
		similarity.experiment = experiment;

		String alignmentMoleculePath = "tmp/file_out.pdbqt";
		File staticFile = new File(alignmentMoleculePath);
		staticFile.createNewFile();
		
		aligner.alignDeployments(similarity);

		alignmentConfigurationFileIsCreatedCorrectly();

		verify(engine, launcher, dbFiles, mapsNameExtractor);
	}

	private void alignmentConfigurationFileIsCreatedCorrectly() {
		String dpfFile = engine.getDpf();
		String dpfTestFile = "test-files/AD4_2file.dpf";
		FileAssert.assertEquals(new File(dpfTestFile), new File(dpfFile));
	}

}
