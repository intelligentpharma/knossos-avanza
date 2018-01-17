package visitors;

import models.ComparisonExperiment;
import models.MapsSimilarities;

import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.test.Fixtures;
import utils.experiment.TestDataCreator;

public class PholusInputGeneratorTest extends FailingMethodsVisitorTest {

	private ComparisonExperiment experiment;

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		TestDataCreator creator = new TestDataCreator();
		experiment = creator.createInverseDockingExperiment();
	}

	@Override
	public KnossosVisitor getVisitor() {
		return new PholusInputGenerator("PHOLUS", true, "activity", 
				"active", "cluster", 0);
	}
	
	@Override
	public int[] getFailingTypes() {
		int[] failingTypes =  {MOLECULE_DATABASE, PHYSICAL_SIMILARITIES, DEPLOYMENT, SCORING, 
				QSAR_EXPERIMENT, QSAR_RESULT};
		return failingTypes;
	}
	
	@Test
	public void getTrainingType(){
		PholusInputGenerator generator = new PholusInputGenerator("PHOLUS", true, "activity", 
				"active", "cluster", 0);
		assertEquals(0, generator.getTrainingType());
	}
    
	@Test
	public void alignmentWithActiveTarget(){
		MapsSimilarities maps = (MapsSimilarities)experiment.alignments.get(3);
		
		PholusInputGenerator generator = new PholusInputGenerator("PHOLUS", true, "activity", 
				"active", "cluster", 0);
		generator.visit(maps);
		
		String expected = "1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 1 cluster1\n";
		assertEquals(expected, generator.getInputData());
	}
	
	@Test
	public void alignmentWithActiveProbe(){
		MapsSimilarities maps = (MapsSimilarities)experiment.alignments.get(0);
		
		PholusInputGenerator generator = new PholusInputGenerator("PHOLUS", false, "activity", 
				"active", "cluster", 0);
		generator.visit(maps);
		
		String expected = "1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 1 cluster1\n";
		assertEquals(expected, generator.getInputData());
	}

	@Test
	public void alignmentWithInactiveDeployments(){
		MapsSimilarities maps = (MapsSimilarities)experiment.alignments.get(3);
	
		PholusInputGenerator generator = new PholusInputGenerator("PHOLUS", false, "activity", 
				"active", "cluster", 0);
		generator.visit(maps);
		
		String expected = "1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 0 cluster1\n";
		assertEquals(expected, generator.getInputData());
	}

	@Test
	public void alignmentWithoutCluster(){
		MapsSimilarities maps = (MapsSimilarities)experiment.alignments.get(2);
		
		log(maps);
	
		PholusInputGenerator generator = new PholusInputGenerator("PHOLUS", false, "activity", 
				"active", "cluster", 0);
		generator.visit(maps);
		
		String expected = "1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 1 \n";
		assertEquals(expected, generator.getInputData());
	}

	private void log(MapsSimilarities maps) {
		Logger.info("Target: Activity '%s', Cluster '%s'", 
				maps.targetDeployment.getPropertyValue("activity"),
				maps.targetDeployment.getPropertyValue("cluster"));
		Logger.info("Probe: Activity '%s', Cluster '%s'", 
				maps.probeDeployment.getPropertyValue("activity"),
				maps.probeDeployment.getPropertyValue("cluster"));
	}


	@Test
	public void fullExperimentConversionWithClusteredProbes(){
		
		String expected = 
			"1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 1 cluster1\n"+
			"1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 1 cluster2\n"+
			"1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 1 \n"+
			"1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 0 cluster1\n"+
			"1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 0 cluster2\n"+
			"1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 0 \n";
		
		PholusInputGenerator generator = new PholusInputGenerator("PHOLUS", false, "activity", 
				"active", "cluster", 0);
		generator.visit(experiment);

		assertEquals(expected, generator.getInputData());
		assertEquals(experiment, generator.getExperiment());
	}

	@Test
	public void fullExperimentConversionWithClusteredTargets(){
		
		String expected = 
			"1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 1 cluster1\n"+
			"1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 1 cluster1\n"+
			"1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 1 cluster1\n"+
			"1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 1 cluster1\n"+
			"1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 1 cluster1\n"+
			"1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 1 cluster1\n";
		
		PholusInputGenerator generator = new PholusInputGenerator("PHOLUS", true, "activity", 
				"active", "cluster", 0);
		generator.visit(experiment);

		assertEquals(expected, generator.getInputData());
	}

	@Test
	public void nameStoredCorrectly(){
		PholusInputGenerator generator = new PholusInputGenerator("PHOLUS", true, "activeColumn", 
				"activeValue", "clusterColumn", 0);
		
		assertEquals("PHOLUS", generator.getName());
	}
	
	@Test
	public void doesNotVisitAlignmentsWithError(){
		MapsSimilarities maps = new MapsSimilarities();
		maps.error = true;
		
		PholusInputGenerator generator = new PholusInputGenerator("PHOLUS", false, "activity", 
				"active", "cluster", 0);
		generator.visit(maps);
		
		assertEquals("", generator.getInputData());
	}
}
