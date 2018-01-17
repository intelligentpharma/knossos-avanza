package engine.factory;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import models.ComparisonExperiment;
import models.Deployment;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import utils.Factory;
import engine.Engine;
import engine.bidimensional.BidimensionalEngine;
import engine.bidimensional.LingosComparisonEngine;
import engine.tridimensional.docking.DockingEngine;
import engine.tridimensional.docking.FakeDockingEngine;
import engine.tridimensional.docking.FakeInverseDockingEngine;
import engine.tridimensional.docking.InverseDockingEngine;

public class EngineFactoryImplTest extends UnitTest {

	EngineFactoryImpl factory;
	Factory toolsFactory;
	
	Deployment target, probe;

	@Before
	public void setup() {
		factory = new EngineFactoryImpl();
		toolsFactory = createMock(Factory.class);
		factory.setFactory(toolsFactory);
		
		expect(toolsFactory.getDatabaseFiles()).andReturn(null).anyTimes();
		expect(toolsFactory.getExternalScriptLauncher()).andReturn(null).anyTimes();
		expect(toolsFactory.getFileUtils()).andReturn(null).anyTimes();
		expect(toolsFactory.createSmilesDataExtractor()).andReturn(null).anyTimes();
		expect(toolsFactory.getFileFormatTranslator()).andReturn(null).anyTimes();
		//expect(toolsFactory.()).andReturn(null).anyTimes();
		replay(toolsFactory);
	}
	
	@Test
	public void createsAutodockVinaEngine(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = Factory.AUTODOCK_VINA;
		Engine engine = factory.getEngine(experiment);
		assertNotNull(engine);
		assertTrue(engine instanceof InverseDockingEngine);
	}
	
	@Test
	public void createsAutodock4Engine(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = Factory.AUTODOCK4;
		Engine engine = factory.getEngine(experiment);
		assertNotNull(engine);
		assertTrue(engine instanceof InverseDockingEngine);
	}
	
	@Test
	public void createsInverseADImprovedEngine(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = Factory.INVERSE_AD_IMPROVED;
		Engine engine = factory.getEngine(experiment);
		assertNotNull(engine);
		assertTrue(engine instanceof InverseDockingEngine);
	}
	
	@Test
	public void createsFakeInverseDockingEngine(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = Factory.FAKE_INVERSE_DOCKING;
		Engine engine = factory.getEngine(experiment);
		assertNotNull(engine);
		assertTrue(engine instanceof FakeInverseDockingEngine);
	}
	
	@Test
	public void createsSeleneEngine(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = Factory.SELENE;
		Engine engine = factory.getEngine(experiment);
		assertNotNull(engine);
		assertTrue(engine instanceof DockingEngine);
	}
	
	@Test
	public void createsSelene4_2_3_Engine(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = Factory.SELENE_AUTODOCK4_2_3;
		Engine engine = factory.getEngine(experiment);
		assertNotNull(engine);
		assertTrue(engine instanceof DockingEngine);
	}
	
	@Test
	public void createsSeleneVinaEngine(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = Factory.SELENE_VINA;
		Engine engine = factory.getEngine(experiment);
		assertNotNull(engine);
		assertTrue(engine instanceof DockingEngine);
	}
	
	@Test
	public void createsFakeDockingEngine(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = Factory.FAKE_DOCKING;
		Engine engine = factory.getEngine(experiment);
		assertNotNull(engine);
		assertTrue(engine instanceof FakeDockingEngine);
	}
	
	@Test(expected = RuntimeException.class)
	public void throwsExceptionForInvalidEngineType(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = 2132;
		factory.getEngine(experiment);
	}
	
	@Test
	public void createsPegassusEngine(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = Factory.LINGO_SIM;
		BidimensionalEngine engine = factory.get2DEngine(experiment);
		assertNotNull(engine);
		assertTrue(engine instanceof LingosComparisonEngine);
	}
	
	@Test(expected = RuntimeException.class)
	public void throwsExceptionForInvalid2DEngineType(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.engineId = 654;
		factory.get2DEngine(experiment);
	}
}
