package engine.tridimensional.peparator;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import models.Deployment;
import models.MoleculeDatabase;
import models.User;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;
import utils.FactoryImpl;
import utils.TemplatedConfiguration;
import utils.database.DatabasePopulationUtils;
import engine.EngineMediator;
import engine.factory.FileNameFactory;
import engine.tridimensional.preparator.DynamicDeploymentPreparator;
import engine.tridimensional.preparator.StaticDeploymentPreparator;
import files.FileUtils;
import files.formats.sdf.MoleculeParserSDFException;

public class DeploymentPreparatorTest extends UnitTest {

	StaticDeploymentPreparator staticPreparator;
	DynamicDeploymentPreparator dynamicPreparator;
	
	FileUtils fileUtils;
	
	static String slurmDir;
	static String pythonScript;

	static MoleculeDatabase database;

	@BeforeClass
	public static void setupClass() {
		slurmDir = TemplatedConfiguration.get("tmp.dir");
		pythonScript = TemplatedConfiguration.get("pythonsh");
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		User aperreau = User.findByUserName("aperreau");
		Factory toolsFactory = new FactoryImpl();
		DatabasePopulationUtils utils = toolsFactory.getDatabasePopulationUtils();
		try{
			database = utils.createMoleculeDatabase("testDB", "test-files/example_prop.sdf", aperreau);
		}
		catch (MoleculeParserSDFException E)
		{
			//Do nothing, loaded DBs will not have non-allowed chats
		}
	}
	
	@Before
	public void setup() {
		fileUtils = createMock(FileUtils.class);
		staticPreparator = new StaticDeploymentPreparator();
		staticPreparator.setFileUtils(fileUtils);
		dynamicPreparator = new DynamicDeploymentPreparator();
		dynamicPreparator.setFileUtils(fileUtils);
	}

	@Test
	public void prepareStaticGasteigerMoleculeTest() {
		Deployment deployment = new Deployment();

		FileNameFactory fileNameFactory = createMock(FileNameFactory.class);
		expect(fileNameFactory.getTargetSdf()).andReturn("target.sdf").times(2);
		expect(fileNameFactory.getOriginalTargetFile()).andReturn("target.sdf");
		expect(fileNameFactory.getTargetPdbqt()).andReturn("target.pdbqt").once();

		EngineMediator engine = createMock(EngineMediator.class);
		engine.convertToStaticPdbqt("target.sdf", "target.pdbqt", Factory.GASTEIGER);
		
		staticPreparator.setFileNameFactory(fileNameFactory);
		staticPreparator.setEngine(engine);

		fileUtils.copyFile("target.sdf", "target.sdf");

		replay(fileNameFactory, engine, fileUtils);

		staticPreparator.prepare(deployment, Factory.GASTEIGER);
		verify(fileNameFactory, engine, fileUtils);
	}
	@Test
	public void prepareDynamicGasteigerMoleculeTest() {
		Deployment deployment = new Deployment();

		FileNameFactory fileNameFactory = createMock(FileNameFactory.class);
		expect(fileNameFactory.getProbeSdf()).andReturn("target.sdf").times(2);
		expect(fileNameFactory.getOriginalProbeFile()).andReturn("target.sdf");
		expect(fileNameFactory.getProbePdbqt()).andReturn("target.pdbqt").once();

		EngineMediator engine = createMock(EngineMediator.class);
		engine.convertToDynamicPdbqt("target.sdf", "target.pdbqt", Factory.GASTEIGER);

		fileUtils.copyFile("target.sdf", "target.sdf");
		
		dynamicPreparator.setFileNameFactory(fileNameFactory);
		dynamicPreparator.setEngine(engine);

		replay(fileNameFactory, engine, fileUtils);

		dynamicPreparator.prepare(deployment, Factory.GASTEIGER);
		verify(fileNameFactory, engine, fileUtils);
	}

	@Test
	public void prepareStaticWithPdbqtDeploymentsCopiesWithoutConverting(){
		Deployment deployment = new Deployment();
		deployment.type = "PDBQT";
		
		FileNameFactory fileNameFactory = createMock(FileNameFactory.class);
		expect(fileNameFactory.getOriginalProbeFile()).andReturn("target.pdbqt");
		expect(fileNameFactory.getProbePdbqt()).andReturn("target.pdbqt").once();
		
		fileUtils.copyFile("target.pdbqt", "target.pdbqt");

		dynamicPreparator.setFileNameFactory(fileNameFactory);
		replay(fileNameFactory, fileUtils);

		dynamicPreparator.prepare(deployment, Factory.GASTEIGER);
		
		verify(fileNameFactory, fileUtils);
		
	}

	@Test
	public void prepareDynamicWithPdbqtDeploymentsCopiesWithoutConverting(){
		Deployment deployment = new Deployment();
		deployment.type = "PDBQT";
		
		FileNameFactory fileNameFactory = createMock(FileNameFactory.class);
		expect(fileNameFactory.getOriginalTargetFile()).andReturn("target.pdbqt");
		expect(fileNameFactory.getTargetPdbqt()).andReturn("target.pdbqt").once();
		
		fileUtils.copyFile("target.pdbqt", "target.pdbqt");

		staticPreparator.setFileNameFactory(fileNameFactory);
		replay(fileNameFactory, fileUtils);

		staticPreparator.prepare(deployment, Factory.GASTEIGER);
		
		verify(fileNameFactory, fileUtils);
		
	}
}
