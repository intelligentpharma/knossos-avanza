package engine.tridimensional.docking;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;

import models.Alignment;
import models.MapsSimilarities;
import models.PhysicalMaps;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import utils.Factory;
import engine.tridimensional.docking.FakeInverseDockingEngine;
import files.DatabaseFiles;

public class FakeInverseDockingEngineTest extends UnitTest {

	FakeInverseDockingEngine engine;

	@Before
	public void setup() {
		engine = new FakeInverseDockingEngine();
	}

	@Test
	public void nameAndIdAreCorrect(){
		assertEquals(Factory.FAKE_INVERSE_DOCKING, engine.id);
		assertEquals(Factory.FAKE_INVERSE_DOCKING_ENGINE_NAME, engine.toString());
	}
	
	@Test
	public void stubbedMethodsDoNotBreak(){
		engine.prepareTargetDeployment();
		engine.setExperiment(null);
		engine.setTargetDeployment(null);
	}
	
	@Test
	public void incrementalNumbersAreSetToSimilarity(){
		MapsSimilarities similarities = new MapsSimilarities();
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		dbFiles.store(anyObject(Alignment.class), anyObject(File.class));
		engine.setDatabaseFiles(dbFiles);
		replay(dbFiles);
		
		engine.calculate(similarities);
		for(String map : PhysicalMaps.getMapNames()){
			double mapValue = similarities.similarities.getMapValue(map);
			assertTrue(mapValue >= 0.0);
			assertTrue(mapValue <= 22.0);
		}
	}
	
	@Test
	public void alignmentFileIsStoredForSimilarity(){
		Alignment similarities = new MapsSimilarities();
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		dbFiles.store(anyObject(Alignment.class), anyObject(File.class));
		engine.setDatabaseFiles(dbFiles);
		replay(dbFiles);
		
		engine.calculate(similarities);

		verify(dbFiles);
	}

	@Test
	public void toStringReturnsName(){
		assertEquals(Factory.FAKE_INVERSE_DOCKING_ENGINE_NAME, engine.toString());
	}
	
	@Test
	public void createsPhysicalSimilarities(){
		assertTrue(engine.createSimilarityModel() instanceof MapsSimilarities);
	}
	
}
