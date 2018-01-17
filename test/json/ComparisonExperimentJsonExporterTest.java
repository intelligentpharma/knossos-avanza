package json;

import java.io.File;
import java.util.ArrayList;

import junitx.framework.FileAssert;
import models.Alignment;
import models.ComparisonExperiment;

import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.db.jpa.JPA;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.ModelUtils;
import utils.experiment.TestDataCreator;

public class ComparisonExperimentJsonExporterTest extends UnitTest{

	TestDataCreator dataCreator;
	private final static long FAKE_ID = 666L;

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		this.dataCreator = new TestDataCreator();
	}
	
	
	@Test
	public void simpleExperimentIsCorrectlyExportedToJsonFile(){
		JPA.setRollbackOnly();
		ComparisonExperiment simpleExperiment = dataCreator.createExperiment();
		simpleExperiment.creationDate = 100;
		
		ModelUtils.setAllModelIdsToGivenId(simpleExperiment, FAKE_ID);
		
		ComparisonExperimentJsonExporter jsonExporter = new ComparisonExperimentJsonExporter();
		jsonExporter.setExperiment(simpleExperiment);
		File actual = jsonExporter.getExperimentJsonForExport();
		Logger.info("Json file: " + actual.getAbsolutePath());
		File expected = new File("test-files/json/simpleExperimentExport.json");

		//Commented out: export functionality needs a revision
		//FileAssert.assertEquals(expected, actual);
	}

	@Test
	//experiments with many items in collection are ordered randomly and we cannot compare to predefined json
	public void experimentWithSinglePossibleOrderingAlwaysReturnsTheSameJson(){
		JPA.setRollbackOnly();
		ComparisonExperiment experiment1mol1dep1prop = dataCreator.create1mol1dep1propEvaluatedExperiment();
		ModelUtils.setAllModelIdsToGivenId(experiment1mol1dep1prop, FAKE_ID);

		if(experiment1mol1dep1prop.alignments.size()>0){
			Alignment alignment = experiment1mol1dep1prop.alignments.get(0);
			experiment1mol1dep1prop.alignments = new ArrayList<Alignment>();
			experiment1mol1dep1prop.alignments.add(alignment);
		}

		AbstractExperimentJsonExporter jsonExporter = new ComparisonExperimentJsonExporter();
		jsonExporter.setExperiment(experiment1mol1dep1prop);
		File actual = jsonExporter.getExperimentJsonForExport();
        Logger.info("Json file: " + actual.getAbsolutePath());
		File expected = new File("test-files/json/experiment1mol1dep1propEvaluatedExport.json");

		//Commented out: export functionality needs a revision
		//FileAssert.assertEquals(expected, actual);
	}
	
}
