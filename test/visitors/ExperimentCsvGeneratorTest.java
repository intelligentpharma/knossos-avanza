package visitors;

import models.Alignment;
import models.ComparisonExperiment;
import models.Deployment;
import models.MapsSimilarities;
import models.MapsSimilaritiesTest;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;

public class ExperimentCsvGeneratorTest extends FailingMethodsVisitorTest {
	
	ExperimentCsvGenerator generator;
	
	@Before
	public void setup(){
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		boolean unique = false;
		generator = new ExperimentCsvGenerator(unique);
	}

	@Override
	public KnossosVisitor getVisitor() {
		boolean unique = false;
		return new ExperimentCsvGenerator(unique);
	}

	@Override
	public int[] getFailingTypes() {
		int[] failingTypes =  {MOLECULE_DATABASE, PHYSICAL_SIMILARITIES, DEPLOYMENT, SCORING, QSAR_EXPERIMENT, QSAR_RESULT};
		return failingTypes;
	}
	
	@Test
	public void toCsvStringCorrectness(){
		MapsSimilarities maps = MapsSimilaritiesTest.getAllOnesMapSimilarities();
		Deployment target = new Deployment();
		target.name="target1";
		Deployment probe = new Deployment();
		probe.name="probe1";
		maps.targetDeployment = target;
		maps.probeDeployment = probe;
		
		generator.visit(maps);
		
		assertEquals("target1,probe1,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0\n",
				generator.getCsv());
	}

	@Test
	public void toCsvStringCorrectnessWithDifferentValues(){
		MapsSimilarities maps = MapsSimilaritiesTest.getAllOnesMapSimilarities();
		maps.similarities.A = 1;
		maps.similarities.Br = 2;
		maps.similarities.C = 3;
		maps.similarities.Ca = 4;
		maps.similarities.Cl = 5;
		maps.similarities.F = 6;
		maps.similarities.Fe = 7;
		maps.similarities.HD = 8;
		maps.similarities.I = 9;
		maps.similarities.Mg = 10;
		maps.similarities.Mn = 11;
		maps.similarities.N = 12;
		maps.similarities.NA = 13;
		maps.similarities.NS = 14;
		maps.similarities.OA = 15;
		maps.similarities.OS = 16;
		maps.similarities.P = 17;
		maps.similarities.S = 18;
		maps.similarities.SA = 19;
		maps.similarities.Zn = 20;
		maps.similarities.d = 21;
		maps.similarities.e = 22;

		Deployment target = new Deployment();
		target.name="target1";
		Deployment probe = new Deployment();
		probe.name="probe1";
		maps.targetDeployment = target;
		maps.probeDeployment = probe;

		generator.visit(maps);

		assertEquals("target1,probe1,1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,17.0,18.0,19.0,20.0,21.0,22.0\n",
				generator.getCsv());
	}
	
	@Test
	public void csvMapsCreatedCorrectly(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		addEmptyMapsSimilarity(experiment);
		addEmptyMapsSimilarity(experiment);
		String expected = 
			"target1,probe1,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0\n"+
			"target1,probe1,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0\n";
		
		generator.visit(experiment);

		assertEquals(expected, generator.getCsv());
	}

	private void addEmptyMapsSimilarity(ComparisonExperiment experiment) {
		Alignment maps = new MapsSimilarities();
		Deployment target = new Deployment();
		target.name="target1";
		Deployment probe = new Deployment();
		probe.name="probe1";
		maps.targetDeployment = target;
		maps.probeDeployment = probe;
		experiment.alignments.add(maps);
	}
	
}
