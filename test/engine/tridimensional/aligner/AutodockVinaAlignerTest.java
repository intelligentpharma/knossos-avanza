package engine.tridimensional.aligner;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.IOException;

import models.Alignment;
import models.AlignmentBox;
import models.Deployment;
import models.ComparisonExperiment;
import models.MapsSimilarities;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.scripts.ExternalScript;
import utils.scripts.SlurmExternalScript;
import engine.EngineMediator;
import engine.tridimensional.aligner.AutodockVinaAligner;
import files.DatabaseFiles;

public class AutodockVinaAlignerTest extends UnitTest {

	AutodockVinaAligner aligner;

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		aligner = new AutodockVinaAligner();
	}

	@Test
	public void calculateInverseVinaWithMocks() throws IOException {
		String autodockCommandKey = "vina_inverse";
		calculateWithMocks(autodockCommandKey);
	}

	@Test
	public void calculateVinaWithMocks() throws IOException {
		String autodockCommandKey = "vina";
		calculateWithMocks(autodockCommandKey);
	}

	private void calculateWithMocks(String autodockCommandKey) throws IOException {
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.exhaustiveness = 8;
		Deployment probe = new Deployment();
		Deployment target = new Deployment();
		Alignment similarity = new MapsSimilarities();
		similarity.probeDeployment = probe;
		similarity.targetDeployment = target;
		similarity.experiment = experiment;

		EngineMediator engine = createMock(EngineMediator.class);
		expect(engine.getTargetPdbqt()).andReturn("tmp/target.pdbqt").anyTimes();
		expect(engine.getProbePdbqt()).andReturn("tmp/probe.pdbqt").anyTimes();
		expect(engine.getOutputPdbqt()).andReturn("tmp/output.pdbqt").anyTimes();
		expect(engine.getTargetMapsPrefix()).andReturn("tmp/inverse.maps").anyTimes();

		AlignmentBox deploymentInfo = new AlignmentBox(1, 2, 3, 4, 5, 6);
		expect(engine.getTargetBox()).andReturn(deploymentInfo).anyTimes();

		ExternalScript launcher = createMock(ExternalScript.class);
		expect(
				launcher.paralelize("vina", SlurmExternalScript.DEFAULT_PARTITION, "./ext/bin/" + autodockCommandKey
						+ " --out tmp/output.pdbqt --receptor tmp/target.pdbqt --ligand tmp/probe.pdbqt "
						+ "--center_x 1.000 --center_y 2.000 --center_z 3.000 "
						+ "--size_x 1.500 --size_y 1.875 --size_z 2.250 --exhaustiveness 8 --cpu 1 ")).andReturn("ok").once();
		expect(launcher.paralelize(1, "pdbqtSplit", "./scripts/pdbqt_split.pl tmp/output.pdbqt")).andReturn("ok").once();

		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		dbFiles.store(anyObject(Alignment.class), anyObject(File.class));
		expectLastCall().anyTimes();
		expect(dbFiles.getFileName(anyObject(Alignment.class))).andReturn("alignmentFile").anyTimes();

		replay(engine, launcher, dbFiles);

		String probeMoleculePath = "tmp/output.pdbqt";
		File staticFile = new File(probeMoleculePath);
		staticFile.createNewFile();

		aligner.autodockCommandKey = autodockCommandKey;
		aligner.setEngine(engine);
		aligner.setLauncher(launcher);
		aligner.setDatabaseFiles(dbFiles);

		aligner.alignDeployments(similarity);

		verify(engine, launcher, dbFiles);
	}

}
