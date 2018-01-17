package controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.AssertTrue;

import models.Event;
import models.MoleculeDatabase;

import org.junit.Before;
import org.junit.Test;

import files.DatabaseFiles;

import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import utils.Factory;
import utils.FactoryImpl;
import utils.database.DatabasePopulationUtils;
import utils.experiment.TestDataCreator;

public class MoleculeDatabaseManagerAsyncTest extends FunctionalTest {

    private static DatabasePopulationUtils utils;
    static TestDataCreator dataCreator;
    Factory toolsFactory;
    
    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        toolsFactory = new FactoryImpl();
        utils = toolsFactory.getDatabasePopulationUtils();
        dataCreator = new TestDataCreator();
    }

	@Test
	public void boxPlotFileIsGenerated() throws InterruptedException, IOException {
        String username = "dbermudez";
        MoleculeDatabaseManagerTest.createDatabaseWithProperties(username);
        
        List<Map<String, String>> databases = MoleculeDatabaseManagerTest.getMoleculeDatabasesOwnedBy(username);
        for (int i = 0; i < databases.size(); i++) {
        	String databaseId = databases.get(i).get("id");
            String databaseName = "Small Database Testing";
			if (databases.get(i).get("name").equalsIgnoreCase(databaseName)) {
            	System.out.println("/moleculeDB/" + databaseId + "/boxPlot?username=dbermudez&selectedDescriptors=TORSDO");
        		Response response = POST("/moleculeDB/" + databaseId + "/boxPlot?username=dbermudez&selectedDescriptors=TORSDO");
        		assertIsOk(response);
        		Thread.sleep(4000);
        		DatabaseFiles databaseFiles = toolsFactory.getDatabaseFiles();
        		MoleculeDatabase moleculeDatabase = new MoleculeDatabase();
        		moleculeDatabase.id = new Long(databaseId);
        		assertTrue(databaseFiles.retrieveBoxplotFile(moleculeDatabase).exists());
            }
        }
	}
	
}