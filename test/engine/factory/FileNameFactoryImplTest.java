package engine.factory;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import models.ComparisonExperiment;
import models.Deployment;
import models.MoleculeDatabase;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import files.DatabaseFiles;

public class FileNameFactoryImplTest extends UnitTest {

	FileNameFactoryImpl factory;
	DatabaseFiles dbFiles;
	
	Deployment target, probe;

	@Before
	public void setup() {
		factory = new FileNameFactoryImpl();
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.id = 1L;
		factory.setExperiment(experiment);
		target = new Deployment();
		target.id = 2L;
		//target.file = new File("test-files/test2.pdbqt");
		probe = new Deployment();
		probe.id = 3L;
		//probe.file = new File("test-files/test2.pdbqt");		
		dbFiles = createMock(DatabaseFiles.class);
		factory.setDatabaseFiles(dbFiles);
		MoleculeDatabase database = new MoleculeDatabase();
		database.id = 4L;
		factory.setDatabase(database);

	}
	
	@Test
	public void probePdbqtExperiment(){
		factory.setExperimentTarget(target);
		factory.setExperimentProbe(probe);
		assertEquals("./tmp/1/3_probe.pdbqt", factory.getProbePdbqt());
	}

	@Test
	public void probePdbqtDatabase(){
		factory.setDatabaseTarget(target);
		factory.setDatabaseProbe(probe);
		assertEquals("./tmp/4/3_probe.pdbqt", factory.getProbePdbqt());
	}

	@Test
	public void targetPdbqtExperiment(){
		factory.setExperimentTarget(target);
		factory.setExperimentProbe(probe);
		assertEquals("./tmp/1/2_target.pdbqt", factory.getTargetPdbqt());
	}
	
	@Test
	public void targetPdbqtDatabase(){
		factory.setDatabaseTarget(target);
		factory.setDatabaseProbe(probe);
		assertEquals("./tmp/4/2_target.pdbqt", factory.getTargetPdbqt());
	}

	@Test
	public void targetGlg(){
		factory.setExperimentTarget(target);
		factory.setExperimentProbe(probe);
		assertEquals("./tmp/1/2_type.glg", factory.getTargetGlg("type"));
	}
	
	@Test
	public void targetGpf(){
		factory.setExperimentTarget(target);
		factory.setExperimentProbe(probe);
		assertEquals("./tmp/1/2_type.gpf", factory.getTargetGpf("type"));
	}
	
	@Test
	public void targetMapsPrefix(){
		factory.setExperimentTarget(target);
		factory.setExperimentProbe(probe);
		assertEquals("./tmp/1/2_type.maps", factory.getTargetMapsPrefix("type"));
	}
	
	@Test
	public void alignmentGlg(){
		factory.setExperimentTarget(target);
		factory.setExperimentProbe(probe);
		assertEquals("./tmp/1/2_vs_3_type.glg", factory.getAlignmentGlg("type"));
	}
	
	@Test
	public void alignmentGpf(){
		factory.setExperimentTarget(target);
		factory.setExperimentProbe(probe);
		assertEquals("./tmp/1/2_vs_3_type.gpf", factory.getAlignmentGpf("type"));
	}
	
	@Test
	public void alignmentMapsPrefix(){
		factory.setExperimentTarget(target);
		factory.setExperimentProbe(probe);
		assertEquals("./tmp/1/2_vs_3_type.maps", factory.getAlignmentMapsPrefix("type"));
	}

	@Test
	public void dpf(){
		factory.setExperimentTarget(target);
		factory.setExperimentProbe(probe);
		assertEquals("./tmp/1/2_vs_3.dpf", factory.getDpf());
	}
	
	@Test
	public void dlg(){
		factory.setExperimentTarget(target);
		factory.setExperimentProbe(probe);
		assertEquals("./tmp/1/2_vs_3.dlg",factory.getDlg());
	}
	
	@Test
	public void outputPdbqt(){
		factory.setExperimentTarget(target);
		factory.setExperimentProbe(probe);
		assertEquals("./tmp/1/2_vs_3_out.pdbqt", factory.getOutputPdbqt());
	}
	
	@Test
	public void getProbeSdf(){
		factory.setExperimentTarget(target);
		factory.setExperimentProbe(probe);
		assertEquals("./tmp/1/3.sdf", factory.getProbeSdf());
	}
	
	@Test
	public void getOriginalProbeFile() throws Exception {
		factory.setExperimentTarget(target);
		factory.setExperimentProbe(probe);
		String fileName = "probeFileName";
		expect(dbFiles.getFileName(probe)).andReturn(fileName);
		replay(dbFiles);
		assertEquals(fileName, factory.getOriginalProbeFile());
		verify(dbFiles);
	}

	@Test
	public void getTargetSdf() throws Exception {
		factory.setExperimentTarget(target);
		factory.setExperimentProbe(probe);
		assertEquals("./tmp/1/2.sdf", factory.getTargetSdf());
	}
	
	@Test
	public void getOriginalTargetFile() throws Exception {
		factory.setExperimentTarget(target);
		factory.setExperimentProbe(probe);
		String fileName = "targetFileName";
		expect(dbFiles.getFileName(target)).andReturn(fileName);
		replay(dbFiles);
		assertEquals(fileName, factory.getOriginalTargetFile());
		verify(dbFiles);
	}
	
	@Test
	public void getPredictionCsvFileName(){
		factory.setExperimentTarget(target);
		factory.setExperimentProbe(probe);
		long id = 1;
		assertEquals("/prediction_1.csv",factory.getPredictionCsvName(id));
	}
	
	@Test
	public void getMoleculeDatabaseCsvFileName(){
		factory.setExperimentTarget(target);
		factory.setExperimentProbe(probe);
		long id=1;
		assertEquals("/qsarPreprocessOutput1.csv",factory.getMoleculeDatabaseCsvFileName(id));
	}
	
}
