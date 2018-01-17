package engine.tridimensional.docking;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;

import models.Alignment;
import models.PhysicalSimilarities;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import utils.Factory;
import engine.tridimensional.docking.FakeDockingEngine;
import files.DatabaseFiles;

public class FakeDockingEngineTest extends UnitTest {

	FakeDockingEngine engine;

	@Before
	public void setup() {
		engine = new FakeDockingEngine();
	}

	@Test
	public void nameAndIdAreCorrect(){
		assertEquals(Factory.FAKE_DOCKING, engine.id);
		assertEquals(Factory.FAKE_DOCKING_ENGINE_NAME, engine.toString());
	}
	
	@Test
	public void stubbedMethodsDoNotBreak(){
		engine.prepareTargetDeployment();
		engine.setExperiment(null);
		engine.setTargetDeployment(null);
	}
	
	@Test
	public void incrementalNumbersAreSetToSimilarity(){
		PhysicalSimilarities similarities = new PhysicalSimilarities();
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		dbFiles.store(anyObject(Alignment.class), anyObject(File.class));
		engine.setDatabaseFiles(dbFiles);
		replay(dbFiles);
		
		engine.calculate(similarities);
		assertTrue(similarities.energy != 0);
		assertTrue(similarities.entropy != 0);
	}
	
	@Test
	public void alignmentFileIsStoredForSimilarity(){
		Alignment similarities = new PhysicalSimilarities();
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		dbFiles.store(anyObject(Alignment.class), anyObject(File.class));
		engine.setDatabaseFiles(dbFiles);
		replay(dbFiles);
		
		engine.calculate(similarities);
		
		verify(dbFiles);
	}

	@Test
	public void toStringReturnsName(){
		assertEquals(Factory.FAKE_DOCKING_ENGINE_NAME, engine.toString());
	}
	
	@Test
	public void createsPhysicalSimilarities(){
		assertTrue(engine.createSimilarityModel() instanceof PhysicalSimilarities);
	}
	
}
