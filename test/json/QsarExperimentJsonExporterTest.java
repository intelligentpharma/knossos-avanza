package json;

import java.io.File;

import junitx.framework.FileAssert;
import models.QsarExperiment;

import org.junit.Before;
import org.junit.Test;

import play.db.jpa.JPA;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.ModelUtils;
import utils.experiment.TestDataCreator;

public class QsarExperimentJsonExporterTest extends UnitTest{
	
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
		QsarExperiment simpleExperiment = dataCreator.create1mol1dep1propEvaluatedQsarExperiment();
		simpleExperiment.creationDate = 100;
		
		ModelUtils.setAllModelIdsToGivenId(simpleExperiment, FAKE_ID);
		
		QsarExperimentJsonExporter jsonExporter = new QsarExperimentJsonExporter();
		jsonExporter.setExperiment(simpleExperiment);
		File actual = jsonExporter.getExperimentJsonForExport();
		File expected = new File("test-files/json/simpleQsarExperiment.json");

		FileAssert.assertEquals(expected, actual);
	}


}
