package utils;

import models.ComparisonExperiment;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.db.jpa.JPA;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.ModelUtils;
import utils.experiment.TestDataCreator;

public class ModelUtilsTest extends UnitTest {

    User owner;
    TestDataCreator dataCreator;
	
    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        owner = User.findByUserName("aperreau");
        dataCreator = new TestDataCreator();
    }
	
	@Test
	public void allModelsIdAreSetToNull(){
		JPA.setRollbackOnly();
		ComparisonExperiment experiment = dataCreator.getSmallEvaluatedExperiment(owner);
		assertNotNull(experiment.id);
		
		ModelUtils utils = new ModelUtils();
		utils.setAllModelIdsToNull(experiment);
		assertNull(experiment.id);
	}
	
}
