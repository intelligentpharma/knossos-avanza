package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ModelForListing;
import models.MoleculeDatabase;
import models.MoleculeDatabaseGuest;
import models.QsarExperiment;
import models.QsarExperimentGuest;
import models.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import utils.Factory;
import utils.experiment.TestDataCreator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

public class QsarExperimentManagerTest extends FunctionalTest {

	String username;
	User owner;
	TestDataCreator dataCreator;

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		username = "aperreau";
		owner = User.findByUserName(username);
		dataCreator = new TestDataCreator();
	}

	@After
	public void teardown() {
		Fixtures.deleteDatabase();
	}

	@Test
	public void listOfExperimentsReturned() {
		QsarExperiment experiment = dataCreator.createQsarExperiment(owner);
		List<Map<String, String>> list = getExperimentsOwnedBy(username);
		assertEquals(1, list.size());
		Map<String, String> experimentData = list.get(0);
		assertEquals(experiment.name, experimentData.get("name"));
	}

	private List<Map<String, String>> getExperimentsOwnedBy(String username) {
		Response response = GET("/qsarExperiment?username=" + username);
		assertIsOk(response);
		assertContentType("application/json", response);
		String content = getContent(response);
		ObjectMapper jsonParser = new ObjectMapper();
		List<Map<String, String>> experiments = null;
		try {
			experiments = jsonParser.readValue(content, List.class);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error in Json");
		}
		return experiments;
	}

	@Test
	public void experimentContainsAllMainFields() {
		QsarExperiment experiment = dataCreator.createQsarExperiment(owner);
		Map<String, String> experimentData = getExperiment(experiment.id);
		assertNotNull(experimentData.get("id"));
		assertNotNull(experimentData.get("name"));
		assertNotNull(experimentData.get("status"));
		assertNotNull(experimentData.get("creationDate"));
		assertNotNull(experimentData.get("startingDate"));
		assertNotNull(experimentData.get("endDate"));
		assertNotNull(experimentData.get("runTime"));
		assertNotNull(experimentData.get("activityProperty"));
		assertNotNull(experimentData.get("moleculesData"));
		assertNotNull(experimentData.get("results"));
		assertNotNull(experimentData.get("externalSelectionType"));
		assertNotNull(experimentData.get("externalPercentage"));
		assertNotNull(experimentData.get("numberOfLatentValues"));
		assertNotNull(experimentData.get("numPropertiesShown"));
		assertNotNull(experimentData.get("parent"));
		assertNotNull(experimentData.get("qsarType"));
		assertNotNull(experimentData.get("numberOfLatentValues"));
	}

	private Map<String, String> getExperiment(long experimentId) {
		Response response = GET(String.format("/qsarExperiment/%d?username=%s",
				experimentId, username));
		assertIsOk(response);
		assertContentType("application/json", response);
		String content = getContent(response);
		ObjectMapper jsonParser = new ObjectMapper();
		Map<String, String> experiment = null;
		try {
			experiment = jsonParser.readValue(content, Map.class);
		} catch (Exception e) {
			Logger.info(content);
			e.printStackTrace();
			fail("Error in Json");
		}
		return experiment;
	}

	@Test
	public void getExperimentData() {
		QsarExperiment experiment = dataCreator.createQsarExperiment(owner);
		ModelForListing experimentData = getExperimentData(experiment.id,
				username);
		assertEquals(experiment.name, experimentData.name);
	}

	private ModelForListing getExperimentData(long experimentId, String username) {
		Response response = GET("/qsarExperiment/" + experimentId
				+ "?username=" + username);
		assertIsOk(response);
		assertContentType("application/json", response);
		String content = getContent(response);
		Gson gson = new Gson();
		ModelForListing experiment = gson.fromJson(content,
				ModelForListing.class);
		return experiment;
	}

	@Test
	public void experimentIsAddedCorrectly() {
		List<MoleculeDatabase> databases = ModelReadingUtils
				.getMoleculeDatabasesOwnedBy(username);
		Map<String, String> experimentParams = new HashMap<String, String>();
		experimentParams.put("username", username);
		experimentParams.put("experimentName", "Tester");
		experimentParams.put("moleculeDatabaseId", "" + databases.get(0).id);
		experimentParams.put("activityProperty", "activity");
		experimentParams.put("numPropertiesShown", "30");
		experimentParams.put("numberOfExternalSetRepetitions", "1");
		experimentParams.put("qsarType", "" + Factory.QSAR_PLS);
		Response response = POST("/qsarExperiment", experimentParams);
		assertIsOk(response);
		List<QsarExperiment> experiments = ModelReadingUtils
				.getQsarExperimentsOwnedBy(username);
		assertEquals(1, experiments.size());
		long lastId = experiments.get(experiments.size() - 1).id;
		QsarExperiment experiment = QsarExperiment.findById(lastId);
		assertEquals("Tester", experiment.name);
	}

	@Test
	public void cannotIntroduceExperimentWithUnownedParts() {
		List<MoleculeDatabase> unownedDatabases = ModelReadingUtils
				.getMoleculeDatabasesOwnedBy("xarroyo");
		Map<String, String> experimentParams = new HashMap<String, String>();
		experimentParams.put("username", username);
		experimentParams.put("experimentName", "Tester");
		experimentParams.put("moleculeDatabaseId", ""
				+ unownedDatabases.get(0).id);
		experimentParams.put("activityProperty", "activity");
		experimentParams.put("numPropertiesShown", "30");
		experimentParams.put("qsarType", Factory.QSAR_PLS + "");
		POST("/qsarExperiment", experimentParams);
		List<QsarExperiment> experiments = ModelReadingUtils
				.getQsarExperimentsOwnedBy(username);
		assertEquals(0, experiments.size());
	}

	@Test
	public void cannotReadUnownedExperiment() {
		try {
			QsarExperiment experiment = dataCreator.createQsarExperiment(owner);
			Map<String, String> experimentParams = new HashMap<String, String>();
			GET("/experiment/" + experiment.id + "?username="
					+ experimentParams);
			fail("Should throw exception");
		} catch (Exception e) {
		}
	}

	public void experimentIsCorrectlyRenamed() {

		User user = User.findByUserName("lnavarro");
		assertNotNull(user);
		QsarExperiment experiment = dataCreator.createQsarExperiment(user);

		Long id = experiment.id;

		String newName = "new Qsar Name";

		Map<String, String> databaseParams = new HashMap<String, String>();

		databaseParams.put("username", user.username);
		databaseParams.put("name", newName);

		Response response = POST(
				"/qsarExperiment/" + experiment.id + "/rename", databaseParams);
		assertIsOk(response);

		experiment = QsarExperiment.findById(id);
		assertEquals(newName, experiment.name);
	}
	
	@Test
	public void getGuestsReturnGuestsCorrectly() {

		User user = User.findByUserName("lnavarro");
		assertNotNull(user);
		QsarExperiment experiment = dataCreator.createQsarExperiment(user);
		
		String [] guests = {"aperreau", "xmaresma"};
		dataCreator.addGuestsToQsarExperiment(experiment, guests);

		Long id = experiment.id;
		
		Map<String, String> databaseParams = new HashMap<String, String>();

		databaseParams.put("username", user.username);		
		
		Response response = POST(
				"/qsarExperiment/" + experiment.id + "/getGuests", databaseParams);
		assertIsOk(response);

		String guestsStr = getContent(response);
		
		String expected = "aperreau,xmaresma,|" + dataCreator.getAllUsersNotInList();
		
		assertEquals(expected, guestsStr);
	}
	
	@Test
	public void setGuestsSetsGuestsCorrectly() {

		User user = User.findByUserName("lnavarro");
		assertNotNull(user);
		QsarExperiment experiment = dataCreator.createQsarExperiment(user);
		
		String [] guests = {"aperreau", "xmaresma"};
		dataCreator.addGuestsToQsarExperiment(experiment, guests);

		Long id = experiment.id;
		
		Map<String, String> databaseParams = new HashMap<String, String>();

		databaseParams.put("username", user.username);		
		databaseParams.put("guests", "xmaresma,dbermudez");
		
		Response response = POST(
				"/qsarExperiment/" + experiment.id + "/setGuests", databaseParams);
		assertIsOk(response);

		List<QsarExperimentGuest> qsarExperimentGuests = QsarExperimentGuest.findByQsarExperiment(experiment);
		assertEquals(2, qsarExperimentGuests.size());		
		assertEquals(qsarExperimentGuests.get(1).username.username, "xmaresma");
		assertEquals(qsarExperimentGuests.get(0).username.username, "dbermudez");
	}

  	@Test
  	public void changeOwnerCorrectly() {

  		User user = User.findByUserName("aperreau");
  		assertNotNull(user);
  		QsarExperiment experiment = dataCreator.createQsarExperiment(user);
  		
  		assertEquals("aperreau", experiment.owner.username);
  		
  		Long id = experiment.id;
  		
  		Map<String, String> databaseParams = new HashMap<String, String>();

  		databaseParams.put("username", user.username);		
  		databaseParams.put("owner", "xmaresma");
  		
  		Response response = POST(
  				"/qsarExperiment/" + experiment.id + "/changeOwner", databaseParams);
  		assertIsOk(response);
  		
  		List<QsarExperimentGuest> comparisonExperimentGuests = QsarExperimentGuest.findByQsarExperiment(experiment);  		
  		assertEquals(1, comparisonExperimentGuests.size());		  		
  		assertEquals(comparisonExperimentGuests.get(0).username.username, "aperreau");
  		
  		experiment = QsarExperiment.findAllOwnedBy((User)User.findByUserName("xmaresma")).get(0);
  		assertNotNull(experiment);
  		
  		List<MoleculeDatabaseGuest> moleculeDatabaseGuests = MoleculeDatabaseGuest.findByMoleculeDatabase(experiment.molecules);
  		assertEquals(1, moleculeDatabaseGuests.size());		  		
  		assertEquals(moleculeDatabaseGuests.get(0).username.username, "xmaresma");  		
  	}

  	@Test
  	public void guestCannotDoModifyActionsOnQsarExperiment(){
		User user = User.findByUserName("lnavarro");
		assertNotNull(user);
		QsarExperiment experiment = dataCreator.createQsarExperiment(user);
		String [] guests = {"aperreau", "xmaresma"};
		dataCreator.addGuestsToQsarExperiment(experiment, guests);

		String newName = "new Qsar Name";

		Map<String, String> databaseParams = new HashMap<String, String>();
		databaseParams.put("username", "dbermudez");
		databaseParams.put("name", newName);

		try{
			Response response = POST("/qsarExperiment/" + experiment.id + "/rename", databaseParams);
		}
		catch (Exception e){
			if(!e.getMessage().contains("User tried to use an experiment it does not own")){
				fail("Expected exception in guestCannotDoModifyActionsOnQsarExperiment not found");
			}
		}
  	}

 	@Test
  	public void guestCanDoReadActionsOnExperimentOthersNot(){
  		User user = User.findByUserName("aperreau");
  		assertNotNull(user);
 		QsarExperiment experiment = dataCreator.createQsarExperiment(user);
		String [] guests = {"lnavarro", "xmaresma"};
		dataCreator.addGuestsToQsarExperiment(experiment, guests);

		Response response = GET("/qsarExperiment/" + experiment.id + "?username=xmaresma"); //OK
		assertIsOk(response);
		
		try{
			response = GET("/qsarExperiment/" + experiment.id + "?username=dbermudez"); //KO
		}
		catch (Exception e){
			if(!e.getMessage().contains("User tried to use an experiment it does not own")){
				fail("Expected exception in guestCanDoReadActionsOnExperiment not found");
			}
		}
  	}
 	
 	@Test
  	public void everybodyCanDoReadActionsOnCommonExperiment(){
  		User user = User.findByUserName("aperreau");
  		assertNotNull(user);
 		QsarExperiment experiment = dataCreator.createQsarExperiment(user);
		
 		Map<String, String> databaseParams = new HashMap<String, String>();

		databaseParams.put("username", "aperreau");
		databaseParams.put("owner", "common");

		Response response = POST("/qsarExperiment/" + experiment.id + "/changeOwner", databaseParams);
		assertIsOk(response);

		response = GET("/qsarExperiment/" + experiment.id + "?username=lnavarro"); 
		assertIsOk(response);
		response = GET("/qsarExperiment/" + experiment.id + "?username=dbermudez"); 
		assertIsOk(response);

  	}
 	
}