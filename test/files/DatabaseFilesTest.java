package files;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;

import models.Alignment;
import models.ComparisonExperiment;
import models.Deployment;
import models.Experiment;
import models.MapsSimilarities;
import models.Molecule;
import models.MoleculeDatabase;
import models.QsarExperiment;
import models.User;

import org.junit.Before;
import org.junit.Test;

import files.formats.sdf.MoleculeParserSDFException;

import play.test.UnitTest;
import utils.Factory;
import utils.FactoryImpl;
import utils.database.DatabasePopulationUtils;

public class DatabaseFilesTest extends UnitTest {

	DatabaseFiles files;
	FileUtils utils;
	DatabasePopulationUtils dataBasePopulationUtils;
	
	@Before
	public void setup(){
		files = new DatabaseFilesImpl();
		files.setBasePath("/tmp");
		utils = createMock(FileUtils.class);
		files.setFileUtils(utils);
		Factory toolsFactory = new FactoryImpl();
		dataBasePopulationUtils = toolsFactory.getDatabasePopulationUtils();
	}
	
	@Test
	public void getsCorrectAlignmentFile(){
		Alignment alignment = createAlignment(12, 123456789);
		
		assertEquals("/tmp/experiment/12/89/alignment_123456789", files.retrieve(alignment).getAbsolutePath());
	}

	private Alignment createAlignment(long experimentId, long alignmentId) {
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.id = experimentId;
		Alignment alignment = new MapsSimilarities();
		alignment.id = alignmentId;
		alignment.experiment = experiment;
		return alignment;
	}
	
	@Test
	public void alignmentFileStoredInCorrectPlace(){
		Alignment alignment = createAlignment(12, 123456789);
		File file = new File("test-files/test2.sdf");
		assertTrue(file.exists());
		
		utils.copyFile(file.getAbsolutePath(), "/tmp/experiment/12/89/alignment_123456789");
		replay(utils);
		
		files.store(alignment, file);
		
		verify(utils);
	}
	
	@Test
	public void experimentDirectoryIsDeleted(){
		Experiment experiment = new ComparisonExperiment();
		experiment.id = 22L;
		
		utils.deleteDirectory("/tmp/experiment/22");
		replay(utils);
		
		files.delete(experiment);
		
		verify(utils);
	}

	@Test
	public void getsCorrectMoleculeDatabaseFileOfNonPersistentDatabase(){
		MoleculeDatabase database = new MoleculeDatabase();
		database.id = 23L;

		assertEquals("/tmp/db/23/database", files.getFileName(database));
	}
	
	@Test
	public void getsCorrectMoleculeDatabaseFileOfAPersistentDatabase(){
		User owner = new User("xmaresma","blanes", "hola");
		owner.save();
		try{
			MoleculeDatabase database = dataBasePopulationUtils.createMoleculeDatabase("PersistentDb", "test-files/example_prop.sdf", owner);
			database.id = 23L;

			assertEquals("/tmp/db/23/database", files.retrieve(database).getAbsolutePath());
			database.delete();
		}
		catch (MoleculeParserSDFException E)
		{
			//Do nothing, loaded DBs will not have non-allowed chats
		}
				
	}

	@Test
	public void moleculeDatabaseFileStoredIncorrectPlace(){
		MoleculeDatabase database = new MoleculeDatabase();
		database.id = 23L;
		File file = new File("test-files/test2.sdf");
		assertTrue(file.exists());
		
		utils.copyFile(file.getAbsolutePath(), "/tmp/db/23/database");
		replay(utils);
		
		files.store(database, file);
		
		verify(utils);
	}
	
	@Test
	public void moleculeDatabaseDirectoryIsDeleted() {
		MoleculeDatabase moleculeDatabase = new MoleculeDatabase();
		moleculeDatabase.id = 22L;
		
		utils.deleteDirectory("/tmp/db/22");
		replay(utils);
		
		files.delete(moleculeDatabase);
		
		verify(utils);
		
	}

	@Test
	public void getsCorrectDeploymentFile(){
		Deployment deployment = createDeployment(34,9876543);
		
		assertEquals("/tmp/db/34/43/deployment_9876543", files.retrieve(deployment).getAbsolutePath());
	}

	private Deployment createDeployment(long databaseId, long deploymentId) {
		MoleculeDatabase database = new MoleculeDatabase();
		database.id = databaseId;
		Molecule molecule = new Molecule();
		Deployment deployment = new Deployment();
		deployment.id = deploymentId;
		database.addMolecule(molecule);
		molecule.addDeployment(deployment);
		return deployment;
	}
	
	@Test
	public void deploymentFileStoredIncorrectPlace(){
		Deployment deployment = createDeployment(32, 654321);
		File file = new File("test-files/test2.sdf");
		assertTrue(file.exists());
		
		utils.copyFile(file.getAbsolutePath(), "/tmp/db/32/21/deployment_654321");
		replay(utils);
		
		files.store(deployment, file);
		
		verify(utils);
	}

	@Test
	public void elementsWithSmallIdsHavePathWithLeadingZeroes(){
		MoleculeDatabase database = new MoleculeDatabase();
		database.id = 34L;
		Molecule molecule = new Molecule();
		Deployment deployment = new Deployment();
		deployment.id = 3L;
		database.addMolecule(molecule);
		molecule.addDeployment(deployment);
		
		assertEquals("/tmp/db/34/03/deployment_3", files.retrieve(deployment).getAbsolutePath());
	}
	
	@Test
	public void experimentResultsFileNameIsCorrect(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.id = 12L;
		
		assertEquals("/tmp/experiment/12/results", files.getFileName(experiment));
	}
	
	@Test
	public void experimentResultsFileIsRetrievedCorrectly(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.id = 12L;
		
		assertEquals("/tmp/experiment/12/results", files.retrieve(experiment).getAbsolutePath());
	}
	
	@Test
	public void modelGraphicFileIsRetrievedCorrectly(){
		QsarExperiment experiment = new QsarExperiment();
		experiment.id = 12L;
		
		assertEquals("/tmp/experiment/12/qsarFitted.png", files.retrieveModelGraphic(experiment, "QSAR_GRAPHIC_FITTED").getAbsolutePath());
	}
	
	@Test
	public void modelFileIsRetrievedCorrectly(){
		QsarExperiment experiment = new QsarExperiment();
		experiment.id = 12L;
		
		assertEquals("/tmp/experiment/12/qsarModelTrain.RData", files.retrieveModelFile(experiment, "train").getAbsolutePath());		
	}

	@Test
	public void experimentResultsFileIsStroredCorrectly(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.id = 12L;
		
		File file = new File("test-files/test2.sdf");
		assertTrue(file.exists());
		
		utils.copyFile(file.getAbsolutePath(), "/tmp/experiment/12/results");
		replay(utils);
		
		files.store(experiment, file);
		
		verify(utils);
	}
	
	@Test
	public void preprocessLogIsRetrievedCorrectly(){
		MoleculeDatabase database = new MoleculeDatabase();
		database.id = 23L;
		
		assertEquals("/tmp/db/23/preprocess.log", files.retrievePreprocessLog(database).getAbsolutePath());
	}
	
	@Test
	public void preprocessLogIsStoredCorrectly(){
		MoleculeDatabase database = new MoleculeDatabase();
		database.id = 23L;
		
		File file = new File("test-files/test2.sdf");
		assertTrue(file.exists());
		
		utils.copyFile(file.getAbsolutePath(), "/tmp/db/23/preprocess.log");
		replay(utils);
		
		files.storePreprocessLog(database, file);
		
		verify(utils);
	}

	@Test
	public void experimentPathIsCorrect(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.id = 12L;
		
		assertEquals("/tmp/experiment/12", files.getPath(experiment));

		QsarExperiment qsarExperiment = new QsarExperiment();
		qsarExperiment.id = 12L;
		
		assertEquals("/tmp/experiment/12", files.getPath(qsarExperiment));
	}
	
	@Test
	public void experimentBasePathIsCorrect(){
		assertEquals("/tmp/experiment/", files.getExperimentBasePath());
	}

	@Test
	public void qsarExperimentResultsFileNameIsCorrect(){
		QsarExperiment experiment = new QsarExperiment();
		experiment.id = 12L;
		
		assertEquals("/tmp/experiment/12/qsarResult.csv", files.getFileName(experiment));
	}
	
	@Test
	public void getModelGraphicFileNameReturnsCorrectNames(){
		QsarExperiment experiment = new QsarExperiment();
		experiment.id = 12L;

		assertEquals("/tmp/experiment/12/qsarFitted.png", files.getModelGraphicFileName(experiment, "QSAR_GRAPHIC_FITTED"));
		assertEquals("/tmp/experiment/12/qsarFitted1.png", files.getModelGraphicFileName(experiment, "QSAR_TABLE_FITTED"));
		assertEquals("/tmp/experiment/12/qsarFitted_Full.png", files.getModelGraphicFileName(experiment, "QSAR_GRAPHIC_FITTED_FULL"));
		assertEquals("/tmp/experiment/12/qsarFitted_Full1.png", files.getModelGraphicFileName(experiment, "QSAR_TABLE_FITTED_FULL"));
		assertEquals("/tmp/experiment/12/qsarPredicted.png", files.getModelGraphicFileName(experiment, "QSAR_GRAPHIC_PREDICTED"));
		assertEquals("/tmp/experiment/12/qsarPredicted1.png", files.getModelGraphicFileName(experiment, "QSAR_TABLE_PREDICTED"));
		assertEquals("/tmp/experiment/12/qsarValidated.png", files.getModelGraphicFileName(experiment, "QSAR_GRAPHIC_VALIDATED"));
		assertEquals("/tmp/experiment/12/qsarValidated1.png", files.getModelGraphicFileName(experiment, "QSAR_TABLE_VALIDATED"));
		assertEquals("/tmp/experiment/12/qsarParametersEvaluation.png", files.getModelGraphicFileName(experiment, "QSAR_GRAPHIC_PARAMETERS_EVALUATION"));
		assertEquals("/tmp/experiment/12/componentsPlot.png", files.getModelGraphicFileName(experiment, "QSAR_PCA_COMPONENTS"));
		assertEquals("/tmp/experiment/12/scorePlot.png", files.getModelGraphicFileName(experiment, "QSAR_PCA_SCORE"));
		assertEquals("/tmp/experiment/12/loadingPlot.png", files.getModelGraphicFileName(experiment, "QSAR_PCA_LOADING"));
		assertEquals("/tmp/experiment/12/", files.getModelGraphicFileName(experiment, "hola"));
	}
	
	@Test
	public void getModelFileNameReturnsCorrectNames(){
		QsarExperiment experiment = new QsarExperiment();
		experiment.id = 12L;
		
		assertEquals("/tmp/experiment/12/qsarModelTrain.RData", files.getModelFileName(experiment, "train"));
		assertEquals("/tmp/experiment/12/qsarModel.RData", files.getModelFileName(experiment, "full"));
		assertEquals("/tmp/experiment/12/ ", files.getModelFileName(experiment, "hola"));
	}
	
}