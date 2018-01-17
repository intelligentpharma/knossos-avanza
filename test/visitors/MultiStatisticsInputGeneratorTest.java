package visitors;

import java.util.ArrayList;
import java.util.List;

import models.Alignment;
import models.ComparisonExperiment;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import utils.experiment.TestDataCreator;

public class MultiStatisticsInputGeneratorTest extends FailingMethodsVisitorTest {

	@Before
	public void setup(){
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
	}
	
	@Override
	public KnossosVisitor getVisitor() {
		return new MultiStatisticsInputGenerator(new ArrayList<ComparisonExperiment>(), "activity", "active", "cluster");
	}
	
	@Override
	public int[] getFailingTypes() {
		int[] failingTypes =  {MOLECULE_DATABASE, SCORING, QSAR_EXPERIMENT, QSAR_RESULT};
		return failingTypes;
	}
	
	@Test
	public void fileCreatedCorrectlyWithTwoInverseDockingExperiments(){
		TestDataCreator creator = new TestDataCreator();
		User owner = User.findByUserName("aperreau");
		ComparisonExperiment exp1 = creator.getEvaluatedInverseDockingExperimentWithProperties("exp1", owner);
		ComparisonExperiment exp2 = creator.getEvaluatedInverseDockingExperimentWithProperties("exp2", owner);
		List<ComparisonExperiment> experiments = new ArrayList<ComparisonExperiment>();
		experiments.add(exp1);
		experiments.add(exp2);
		MultiStatisticsInputGenerator generator = new MultiStatisticsInputGenerator(experiments,
				"activity", "active", "cluster");
		String input = generator.getInputData();
		String expected = "\"exp1\",\"MOL01\",1,1,\"cluster1\",17.5722\n"+
			"\"exp1\",\"MOL02\",1,1,\"cluster2\",17.5722\n"+
			"\"exp1\",\"MOL03\",1,0,,17.5722\n"+
			"\"exp1\",\"MOL04\",0,1,\"cluster1\",17.5722\n"+
			"\"exp1\",\"MOL05\",0,1,\"cluster2\",17.5722\n"+
			"\"exp1\",\"MOL06\",0,0,,17.5722\n"+
			"\"exp2\",\"MOL01\",1,1,\"cluster1\",17.5722\n"+
			"\"exp2\",\"MOL02\",1,1,\"cluster2\",17.5722\n"+
			"\"exp2\",\"MOL03\",1,0,,17.5722\n"+
			"\"exp2\",\"MOL04\",0,1,\"cluster1\",17.5722\n"+
			"\"exp2\",\"MOL05\",0,1,\"cluster2\",17.5722\n"+
			"\"exp2\",\"MOL06\",0,0,,17.5722\n";
		assertEquals(expected, input);
	}

	@Test
	public void fileCreatedCorrectlyWithTwoDockingExperiments(){
		TestDataCreator creator = new TestDataCreator();
		User owner = User.findByUserName("aperreau");
		ComparisonExperiment exp1 = creator.getEvaluatedDockingExperimentWithProperties("exp1", owner);
		ComparisonExperiment exp2 = creator.getEvaluatedDockingExperimentWithProperties("exp2", owner);
		List<ComparisonExperiment> experiments = new ArrayList<ComparisonExperiment>();
		experiments.add(exp1);
		experiments.add(exp2);
		MultiStatisticsInputGenerator generator = new MultiStatisticsInputGenerator(experiments,
				"activity", "active", "cluster");
		String input = generator.getInputData();
		String expected = "\"exp1\",\"MOL01\",1,1,\"cluster1\",0.123\n"+
			"\"exp1\",\"MOL02\",1,1,\"cluster2\",0.123\n"+
			"\"exp1\",\"MOL03\",1,0,,0.123\n"+
			"\"exp1\",\"MOL04\",0,1,\"cluster1\",0.123\n"+
			"\"exp1\",\"MOL05\",0,1,\"cluster2\",0.123\n"+
			"\"exp1\",\"MOL06\",0,0,,0.123\n"+
			"\"exp2\",\"MOL01\",1,1,\"cluster1\",0.123\n"+
			"\"exp2\",\"MOL02\",1,1,\"cluster2\",0.123\n"+
			"\"exp2\",\"MOL03\",1,0,,0.123\n"+
			"\"exp2\",\"MOL04\",0,1,\"cluster1\",0.123\n"+
			"\"exp2\",\"MOL05\",0,1,\"cluster2\",0.123\n"+
			"\"exp2\",\"MOL06\",0,0,,0.123\n";
		assertEquals(expected, input);
	}
	
	@Test
	public void alignmentsWithErrorAreNotVisited(){
		TestDataCreator creator = new TestDataCreator();
		User owner = User.findByUserName("aperreau");
		ComparisonExperiment exp1 = creator.getEvaluatedDockingExperimentWithProperties("exp1", owner);
		for(Alignment alignment : exp1.alignments){
			alignment.error = true;
		}
		List<ComparisonExperiment> experiments = new ArrayList<ComparisonExperiment>();
		experiments.add(exp1);
		MultiStatisticsInputGenerator generator = new MultiStatisticsInputGenerator(experiments,
				"activity", "active", "cluster");
		String input = generator.getInputData();
		assertEquals("", input);
	}

}
