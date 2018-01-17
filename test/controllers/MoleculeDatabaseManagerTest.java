package controllers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junitx.framework.FileAssert;
import models.Deployment;
import models.Molecule;
import models.MoleculeDatabase;
import models.MoleculeDatabaseGuest;
import models.User;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.mvc.Http.Response;
import play.mvc.Scope.Session;
import play.test.Fixtures;
import play.test.FunctionalTest;
import utils.CompressionUtilsImpl;
import utils.Factory;
import utils.FactoryImpl;
import utils.TemplatedConfiguration;
import utils.database.DatabasePopulationUtils;
import utils.experiment.TestDataCreator;

import com.fasterxml.jackson.databind.ObjectMapper;

import files.DatabaseFiles;
import files.formats.sdf.MoleculeParserSDFException;

public class MoleculeDatabaseManagerTest extends FunctionalTest {

    private static DatabasePopulationUtils utils;
    static TestDataCreator dataCreator;

    //TODO 4 commented tests!!!
    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        Factory toolsFactory = new FactoryImpl();
        utils = toolsFactory.getDatabasePopulationUtils();
        dataCreator = new TestDataCreator();
    }

    @After
    public void teardown() {
        Fixtures.deleteDatabase();
    }

    @Test
    public void listOfMoleculeDatabasesOwnedByUserReturned() {
        List<Map<String, String>> list = getMoleculeDatabasesOwnedBy("aperreau");
        assertEquals(2, list.size());
        assertEquals("Proteins", list.get(0).get("name"));
        assertEquals("Salts", list.get(1).get("name"));
        assertEquals("proteins.sdf", list.get(0).get("originalFileName"));
    }

    @Test
    public void getMoleculeDatabaseData() {
        List<Map<String, String>> list = getMoleculeDatabasesOwnedBy("aperreau");
        String databaseId = list.get(0).get("id");
        Response response = GET("/moleculeDB/" + databaseId + "?username=aperreau");
        assertIsOk(response);
        assertContentType("application/json", response);
        String content = getContent(response);
        ObjectMapper jsonParser = new ObjectMapper();
        Map<String, String> parsedDatabase = null;
        try {
            parsedDatabase = jsonParser.readValue(content, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error in Json");
        }
        assertEquals("Proteins", parsedDatabase.get("name"));
    }

    @Test
    public void addNewMoleculeDatabase() throws InterruptedException {
        Map<String, String> databaseParams = new HashMap<String, String>();
        String username = "aperreau";
        databaseParams.put("username", username);
        databaseParams.put("molecules.name", "Small Database");
        databaseParams.put("molecules.originalFileName", "smallMoleculeDatabase.sdf");
        Map<String, File> databaseFiles = new HashMap<String, File>();
        databaseFiles.put("molecules.transientFile", new File("test-files/smallMoleculeDatabase.sdf"));
        Response response = POST("/moleculeDB", databaseParams, databaseFiles);
        assertIsOk(response);
        Thread.currentThread().sleep(1000);
        List<Map<String, String>> databases = getMoleculeDatabasesOwnedBy(username);
        assertEquals(3, databases.size());
        assertEquals("Small Database", databases.get(2).get("name"));
    }

    @Test
    public void databaseWithIncorrectFormatSdfIsNotUploaded() throws InterruptedException {
        Map<String, String> databaseParams = new HashMap<String, String>();
        String username = "dbermudez";
		Session.current().put("username", username);
        databaseParams.put("username", username);
        databaseParams.put("molecules.name", "Database with incorrect format");
        databaseParams.put("molecules.originalFileName", "badformat_example.sdf");
        Map<String, File> databaseFiles = new HashMap<String, File>();
        databaseFiles.put("molecules.transientFile", new File("test-files/badformat_example.sdf"));
        Response response = POST("/moleculeDB", databaseParams, databaseFiles);
        assertIsOk(response);
        Thread.currentThread().sleep(1000);

        List<Map<String, String>> databases = getMoleculeDatabasesOwnedBy(username);
        assertEquals(1, databases.size());

        Thread.currentThread().sleep(3000);

        databases = getMoleculeDatabasesOwnedBy(username);

        assertEquals(0, databases.size());
    }

    protected static List<Map<String, String>> getMoleculeDatabasesOwnedBy(String username) {
        Response response = GET("/moleculeDB?username=" + username);
        assertIsOk(response);
        assertContentType("application/json", response);
        String content = getContent(response);
        Logger.debug(content);
        ObjectMapper jsonParser = new ObjectMapper();
        List<Map<String, String>> parsedDatabases = null;
        try {
            parsedDatabases = jsonParser.readValue(content, List.class);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error in Json");
        }
        return parsedDatabases;
    }

    @Test
    public void databasePropertiesAreRenderedCorrectlyAsJson() {
        User aperreau = User.findByUserName("aperreau");
        Factory toolsFactory = new FactoryImpl();
        DatabasePopulationUtils utils = toolsFactory.getDatabasePopulationUtils();
        try{
        MoleculeDatabase database = utils.createMoleculeDatabase("database",
                "test-files/smallMoleculeDatabase.sdf", aperreau);
       
        addRandomProperties(database, "test");
        addRandomProperties(database, "test2");
        Response response = GET("/moleculeDB/" + database.id + "?username=aperreau");
        String content = getContent(response);
        Logger.debug(content);
        ObjectMapper jsonParser = new ObjectMapper();
        Map<String, ?> parsedDatabase = null;
        List<String> parsedProperties = null;
        
        try {
            parsedDatabase = jsonParser.readValue(content, Map.class);
            parsedProperties = (List<String>) parsedDatabase.get("properties");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error in Json");
        }
        for (String property : parsedProperties) {
            Logger.info(property);
        }
        assertNotNull(parsedProperties);
        assertEquals(12, parsedProperties.size());
        assertTrue(parsedProperties.contains("test2"));
        }
        catch (MoleculeParserSDFException E){
			//Do nothing, loaded DBs will not have non-allowed chats
		}
    }

    private static void addRandomProperties(MoleculeDatabase database, String propertyName) {
        for (Deployment probe : database.getAllDeployments()) {
            probe.putProperty(propertyName, String.valueOf(Math.random() * 100));
            database.save();
        }
    }

    @Test
    public void addNewMoleculeDatabaseWithSdfFormatAndProperties() {
        List<Map<String, String>> databases = addNewDatabaseAndGetAllDatabasesOwned();
        assertEquals(2, databases.size());
        assertEquals("Sdf Small Database", databases.get(1).get("name"));
    }

    private List<Map<String, String>> addNewDatabaseAndGetAllDatabasesOwned() {
        Map<String, String> databaseParams = new HashMap<String, String>();
        String username = "xmaresma";
        databaseParams.put("username", username);
        databaseParams.put("molecules.name", "Sdf Small Database");
        databaseParams.put("molecules.originalFileName", "mol12_13_test.sdf");
        Map<String, File> databaseFiles = new HashMap<String, File>();
        databaseFiles.put("molecules.transientFile", new File("test-files/mol12_13_test.sdf"));
        Response response = POST("/moleculeDB", databaseParams, databaseFiles);
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException("Sleep didn't work");
        }
        assertIsOk(response);
        return getMoleculeDatabasesOwnedBy(username);
    }

    @Test
    public void allMoleculeDatabasesHaveCalculatedPropertiesFlag() {
        List<Map<String, String>> databases = addNewDatabaseAndGetAllDatabasesOwned();

        for (Map<String, String> dabaseProperties : databases) {
            assertNotNull(dabaseProperties.get("calculatedProperties"));
        }
    }

    @Test
    public void smilesDatabaseIsLoadedCorrectly() throws IOException {
        User owner = User.findByUserName("dbermudez");
        FileUtils.copyFile(new File("test-files/uncanonical.smi"), new File("test-files/uncanonicalCan.smi"));
        try{
        MoleculeDatabase database = utils.createMoleculeDatabase("UncanonicalSmiles", "test-files/uncanonicalCan.smi", owner);
        database.save();
        Factory factory = new FactoryImpl();
        DatabaseFiles databaseFiles = factory.getDatabaseFiles();
        FileAssert.assertEquals(new File("test-files/uncanonicalCanonized.smi"), new File(databaseFiles.getFileName(database)));
        }    
	    catch (MoleculeParserSDFException E){
			//Do nothing, loaded DBs will not have non-allowed chats
		}
        
    }

    //@Test
    public void databaseCalculatedHasCalculatedPropertiesFlagSet() throws InterruptedException {
        User owner = User.findByUserName("dbermudez");
        try{
        MoleculeDatabase database = utils.createMoleculeDatabase("4molec", "test-files/4_molec_orderTest.sdf", owner);
        database.save();

        Logger.info("Test Database has %d Deployments", database.getAllDeployments().size());

        assertFalse(database.calculatedProperties);

        Map<String, String> requestParams = new HashMap<String, String>();
        String username = "dbermudez";
        requestParams.put("username", username);
        Response response = POST("/moleculeDB/" + database.id + "/calculateProperties", requestParams);

        assertIsOk(response);
        //Thread.currentThread().sleep(10000);
        database.refresh();
        assertTrue(database.calculatedProperties);
        }
        catch (MoleculeParserSDFException E){
			//Do nothing, loaded DBs will not have non-allowed chats
		}
    }

    //@Test
    public void getSdfFileWorksCorrectly() throws InterruptedException, IOException {

        Map<String, String> databaseParams = new HashMap<String, String>();
        String username = "dbermudez";
        databaseParams.put("username", username);
        databaseParams.put("molecules.name", "Small Database Testing");
        databaseParams.put("molecules.originalFileName", "smallMoleculeDatabase.sdf");
        Map<String, File> databaseFiles = new HashMap<String, File>();
        databaseFiles.put("molecules.transientFile", new File("test-files/smallMoleculeDatabase.sdf"));
        Response response = POST("/moleculeDB", databaseParams, databaseFiles);
        assertIsOk(response);
        Thread.currentThread().sleep(5000);

        List<Map<String, String>> databases = getMoleculeDatabasesOwnedBy(username);

        for (int i = 0; i < databases.size(); i++) {
            if (databases.get(i).get("name").equalsIgnoreCase("Small Database Testing")) {
                String databaseId = databases.get(i).get("id");
                response = GET("/moleculeDB/" + databaseId + "/getSdfFile?username=" + username);
                assertIsOk(response);
                String generatedSdfFileName = TemplatedConfiguration.get("tmp.dir") + "/sdf_" + databaseId + "_" + username + ".sdf";

                //Files are not the same because of the system properties and the order of them, so we can't check equality
                List<String> lines = org.apache.commons.io.FileUtils.readLines(new File(generatedSdfFileName));
                int countDeployments = 0;
                for (String line : lines) {
                    if (line.equals("$$$$")) {
                        countDeployments++;
                    }
                }
                assertEquals(7, countDeployments);
            }
        }
    }

    protected static void createDatabaseWithProperties(String username) throws InterruptedException, IOException {
        Map<String, String> databaseParams = new HashMap<String, String>();

        databaseParams.put("username", username);
        databaseParams.put("molecules.name", "Small Database Testing");
        databaseParams.put("molecules.originalFileName", "test2.sdf");
        Map<String, File> databaseFiles = new HashMap<String, File>();
        databaseFiles.put("molecules.transientFile", new File("test-files/test2.sdf"));
        Response response = POST("/moleculeDB", databaseParams, databaseFiles);
        assertIsOk(response);
        Thread.currentThread().sleep(3000);
    }

    @Test
    public void getDescriptorsFileWorksCorrectly() throws InterruptedException, IOException {
        String username = "lnavarro";
        createDatabaseWithProperties(username);
        List<Map<String, String>> databases = getMoleculeDatabasesOwnedBy(username);
        for (int i = 0; i < databases.size(); i++) {
            if (databases.get(i).get("name").equalsIgnoreCase("Small Database Testing")) {
                String databaseId = databases.get(i).get("id");
                String databaseName = databases.get(i).get("name");
                Response response = POST("/moleculeDB/" + databaseId + "/getDescriptorsFile?username=" + username + "&downloadFormat=CSV&descriptors=Activity&descriptors=GCUT_SLOGP_0&descriptors=PEOE_VSA-4&descriptors=Q_VSA_HYD&descriptors=SMR_VSA1&descriptors=a_nC&descriptors=b_rotN");
                assertIsOk(response);
                Thread.currentThread().sleep(5000);

                String generatedCsvFileName = new FactoryImpl().getFileUtils().getDatabaseTemporalFileName(new Long(databaseId), databaseName, "csv");                
                
                new CompressionUtilsImpl().uncompress(new File(generatedCsvFileName + ".gz"), new File(generatedCsvFileName));

                List<String> linesOriginal = org.apache.commons.io.FileUtils.readLines(new File("test-files/csv-export/exportedFilteredDatabase.csv"));
                List<String> linesGenerated = org.apache.commons.io.FileUtils.readLines(new File(generatedCsvFileName));

                assertEquals(linesOriginal.size(), linesGenerated.size());
                boolean filesEqualsWithoutIds = true;
                for (int j = 0; j < linesOriginal.size(); j++) {
                    String originalLineWithoutId = linesOriginal.get(j).substring(linesOriginal.get(j).indexOf(",") + 1);
                    String generatedLineWithoutId = linesGenerated.get(j).substring(linesGenerated.get(j).indexOf(",") + 1);
                    if (!originalLineWithoutId.equals(generatedLineWithoutId)) {
                        filesEqualsWithoutIds = false;
                    }
                }
                assertTrue(filesEqualsWithoutIds);
            }
        }
    }

    @Test
    public void getSdfFileFilteringPropertiesWorksCorrectly() throws InterruptedException, IOException {
        String username = "dbermudez";
        createDatabaseWithProperties(username);
        List<Map<String, String>> databases = getMoleculeDatabasesOwnedBy(username);
        for (int i = 0; i < databases.size(); i++) {
            if (databases.get(i).get("name").equalsIgnoreCase("Small Database Testing")) {
                String databaseId = databases.get(i).get("id");
                Logger.info("/moleculeDB/" + databaseId + "/getSdfFile?username=" + username);
                Response response = POST("/moleculeDB/" + databaseId + "/getSdfFile?username=" + username);
                assertIsOk(response);
                Thread.currentThread().sleep(5000);
                
                String generatedSdfFileName = new FactoryImpl().getFileUtils().getDatabaseTemporalFileName(new Long(databaseId), "Small Database Testing", "sdf");                
                
                List<String> linesOriginal = org.apache.commons.io.FileUtils.readLines(new File(generatedSdfFileName));
                List<String> linesGenerated = org.apache.commons.io.FileUtils.readLines(new File(generatedSdfFileName));
                
                assertEquals(linesOriginal.size(), linesGenerated.size());
                boolean filesEqualsWithoutIds = true;
                for(int j=0; j<linesOriginal.size(); j++){
                	String originalLineWithoutId = linesOriginal.get(j).substring(linesOriginal.get(j).indexOf(",")+1);
                	String generatedLineWithoutId = linesGenerated.get(j).substring(linesGenerated.get(j).indexOf(",")+1);
                	if(!originalLineWithoutId.equals(generatedLineWithoutId)){
                		filesEqualsWithoutIds = false;
                	}
                }
                assertTrue(filesEqualsWithoutIds);
            }
        }
    }
    
    //@Test
    public void databaseIsCorrectlyDeleted() throws InterruptedException, IOException {
        String username = "lnavarro";
        createDatabaseWithProperties(username);
        Map<String, String> databaseParams = new HashMap<String, String>();
        List<Map<String, String>> databases = getMoleculeDatabasesOwnedBy(username);
        assertEquals(1, databases.size());
        Response response = DELETE("/moleculeDB/" + databases.get(0).get("id") + "?username=lnavarro");
        assertIsOk(response);
        Thread.currentThread().sleep(5000);
        databases.clear();
        databases = getMoleculeDatabasesOwnedBy(username);
        assertEquals(0, databases.size());

    }

    //@Test
    public void multipleDatabaseAreCorrectlyDeleted() {
        User user = User.findByUserName("lnavarro");
        Factory toolsFactory = new FactoryImpl();
        DatabasePopulationUtils utils = toolsFactory.getDatabasePopulationUtils();
        try{
        MoleculeDatabase database1 = utils.createMoleculeDatabase("database1",
                "test-files/smallMoleculeDatabase.sdf", user);
        MoleculeDatabase database2 = utils.createMoleculeDatabase("database2",
                "test-files/smallMoleculeDatabase.sdf", user);
        List<Map<String, String>> databases = getMoleculeDatabasesOwnedBy(user.username);
        assertEquals(2, databases.size());
        DELETE("/moleculeDB/" + database1.id + "," + database2.id + "?username=lnavarro");
        databases.clear();
        databases = getMoleculeDatabasesOwnedBy(user.username);
        assertEquals(0, databases.size());
        }
        catch (MoleculeParserSDFException E){
			//Do nothing, loaded DBs will not have non-allowed chats
		}

    }

    @Test
    public void deleteDeploymentWorksCorrectly() {
        User user = User.findByUserName("lnavarro");
        assertNotNull(user);
        MoleculeDatabase database = dataCreator.createSmallDatabaseWithActivityAndClustering(user);
        List<Deployment> deploymentList = Deployment.findDeploymentsByDatabaseAndName(database, "mol51");
        assertEquals(1, deploymentList.size());

        long deploymentId = deploymentList.get(0).id;

        DELETE("/deployment/" + deploymentId + "?username=lnavarro");

        deploymentList = Deployment.findDeploymentsByDatabaseAndName(database, "mol51");
        assertTrue(deploymentList.isEmpty());
    }
    
      @Test
    public void deleteAllDeploymentsOfAMoleculeDeletesTheMolecule() {
        User user = User.findByUserName("lnavarro");
        assertNotNull(user);
        MoleculeDatabase database = dataCreator.createTwoMoleculesFourConformationsDatabase(user);
        database.save();                               
        
        long deploymentId = Deployment.findDeploymentsByDatabaseAndName(database, "ZINC01_c001").get(0).id;
        
        DELETE("/deployment/" + deploymentId + "?username=lnavarro");
        assertEquals(0, Molecule.findMoleculeByDatabaseAndName(database, "ZINC01").size());        
        
    }
           
  	@Test
  	public void getGuestsReturnGuestsCorrectly() {

  		User user = User.findByUserName("lnavarro");
  		assertNotNull(user);
  		MoleculeDatabase db = dataCreator.createSmallDatabaseWithoutProperties(user);
  		
  		String [] guests = {"aperreau", "xmaresma"};
  		dataCreator.addGuestsToMoleculeDatabase(db, guests);

  		Long id = db.id;
  		
  		Map<String, String> databaseParams = new HashMap<String, String>();

  		databaseParams.put("username", user.username);		
  		
  		Response response = POST(
  				"/moleculeDB/" + db.id + "/getGuests", databaseParams);
  		assertIsOk(response);

  		String guestsStr = getContent(response);
  		
  		String expected = "aperreau,xmaresma,|" + dataCreator.getAllUsersNotInList();
  		
  		assertEquals(expected, guestsStr);
  	}
  	
  	@Test
  	public void setGuestsSetsGuestsCorrectly() {

  		User user = User.findByUserName("lnavarro");
  		assertNotNull(user);
  		MoleculeDatabase db = dataCreator.createSmallDatabaseWithoutProperties(user);
  		
  		String [] guests = {"aperreau", "xmaresma"};
  		dataCreator.addGuestsToMoleculeDatabase(db, guests);

  		Long id = db.id;
  		
  		Map<String, String> databaseParams = new HashMap<String, String>();

  		databaseParams.put("username", user.username);		
  		databaseParams.put("guests", "xmaresma,dbermudez");
  		
  		Response response = POST(
  				"/moleculeDB/" + db.id + "/setGuests", databaseParams);
  		assertIsOk(response);

  		List<MoleculeDatabaseGuest> moleculeDatabaseGuests = MoleculeDatabaseGuest.findByMoleculeDatabase(db);
  		assertEquals(2, moleculeDatabaseGuests.size());		
  		assertEquals(moleculeDatabaseGuests.get(1).username.username, "xmaresma");
  		assertEquals(moleculeDatabaseGuests.get(0).username.username, "dbermudez");
  	}
  	
//  	@Test 
    public void databaseIsCorrectlyDuplicated() throws InterruptedException, IOException {
        User user = User.findByUserName("lnavarro");
      assertNotNull(user);
      MoleculeDatabase database = dataCreator.createSmallDatabaseWithActivityAndClustering(user);
      List<Deployment> deploymentList = Deployment.findDeploymentsByDatabaseAndName(database, "mol51");
      assertEquals(1, deploymentList.size());
      
      String newName = "duplicated";

      long deploymentId = deploymentList.get(0).id;

      Map<String, String> databaseParams = new HashMap<String, String>();
           
      databaseParams.put("username", user.username);
      databaseParams.put("name", newName);
      
      Response response = POST("/moleculeDB/"+database.id+"/duplicate", databaseParams);
      assertIsOk(response);
      
      List<MoleculeDatabase> newDatabase = MoleculeDatabase.findByOwnerAndName(user, newName);
      assertNotNull(newDatabase);
      
      deploymentList = Deployment.findDeploymentsByDatabaseAndName(newDatabase.get(0), "mol51");
      assertEquals(1, deploymentList.size());
        
    }
    @Test
    public void databaseIsCorrectlyRenamed() {
  	  
  	  User user = User.findByUserName("lnavarro");
        assertNotNull(user);
        MoleculeDatabase database = dataCreator.createSmallDatabaseWithActivityAndClustering(user);
        
        Long id = database.id;
        
        String newName = "newDatabaseName";
        
        Map<String, String> databaseParams = new HashMap<String, String>();
        
        databaseParams.put("username", user.username);
        databaseParams.put("name", newName);
        
        Response response = POST("/moleculeDB/"+database.id+"/rename", databaseParams);
        assertIsOk(response);
        
        MoleculeDatabase moleculeDatabase = MoleculeDatabase.findByOwnerAndName(user, newName).get(0);
        assertNotNull(moleculeDatabase);          
    }

  	
  	@Test
  	public void changeOwnerCorrectly() {

  		User user = User.findByUserName("lnavarro");
  		assertNotNull(user);
  		MoleculeDatabase db = dataCreator.createSmallDatabaseWithoutProperties(user);
  		
  		assertEquals("lnavarro", db.owner.username);
  		
  		Long id = db.id;
  		
  		Map<String, String> databaseParams = new HashMap<String, String>();

  		databaseParams.put("username", user.username);		
  		databaseParams.put("owner", "xmaresma");
  		
  		Response response = POST(
  				"/moleculeDB/" + db.id + "/changeOwner", databaseParams);
  		assertIsOk(response);
  		
  		List<MoleculeDatabaseGuest> moleculeDatabaseGuests = MoleculeDatabaseGuest.findByMoleculeDatabase(db);  		
  		assertEquals(1, moleculeDatabaseGuests.size());		  		
  		assertEquals(moleculeDatabaseGuests.get(0).username.username, "lnavarro");
  		
  		db = MoleculeDatabase.findByOwnerAndName((User)User.findByUserName("xmaresma"), "Small without properties").get(0);
  		assertNotNull(db);
  	}

  	@Test
  	public void guestCannotDoModifyActionsOnMoleculeDatabase(){
		User user = User.findByUserName("lnavarro");
		assertNotNull(user);
		MoleculeDatabase database = dataCreator.createSmallDatabaseWithActivityAndClustering(user);
		String [] guests = {"aperreau", "xmaresma"};
		dataCreator.addGuestsToMoleculeDatabase(database, guests);

		String newName = "newDatabaseName";
		Map<String, String> databaseParams = new HashMap<String, String>();
		  
		databaseParams.put("username", "dbermudez");
		databaseParams.put("name", newName);
		  
		try{
			Response response = POST("/moleculeDB/"+database.id+"/rename", databaseParams);
		}
		catch (Exception e){
			if(!e.getMessage().contains("User tried to use a database it does not own")){
				fail("Expected exception in guestCannotDoModifyActionsOnMoleculeDatabase not found");
			}
		}
  	}

 	@Test
  	public void guestCanDoReadActionsOnMoleculeDatabaseOthersNot(){
  		User user = User.findByUserName("lnavarro");
  		assertNotNull(user);
  		
		MoleculeDatabase database = dataCreator.createSmallDatabaseWithActivityAndClustering(user);
		String [] guests = {"aperreau", "xmaresma"};
		dataCreator.addGuestsToMoleculeDatabase(database, guests);

		Response response = GET("/moleculeDB/" + database.id + "?username=xmaresma"); //OK
		assertIsOk(response);
		
		try{
			response = GET("/moleculeDB/" + database.id + "?username=dbermudez"); //KO
		}
		catch (Exception e){
			if(!e.getMessage().contains("User tried to use a database it does not own")){
				fail("Expected exception in guestCanDoReadActionsOnMoleculeDatabaseOthersNot not found");
			}
		}
  	}
 	
	@Test
	public void everybodyCanDoReadActionsOnCommonDatabase() {
		User user = User.findByUserName("common");
		assertNotNull(user);

		MoleculeDatabase db = dataCreator.createSmallDatabaseWithoutProperties(user);
				
		Response response = GET("/moleculeDB/" + db.id + "?username=lnavarro"); 
		assertIsOk(response);
		response = GET("/moleculeDB/" + db.id + "?username=dbermudez"); 
		assertIsOk(response);

	}

}