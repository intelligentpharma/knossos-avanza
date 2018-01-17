package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ComparisonExperiment;
import models.ComparisonExperimentGuest;
import models.Deployment;
import models.ModelForListing;
import models.MoleculeDatabase;
import models.MoleculeDatabaseGuest;
import models.Ponderation;
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

public class ExperimentManagerTest extends FunctionalTest {

	String username;
	TestDataCreator dataCreator;

	@Before
	public void setup() {
		username = "aperreau";
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		dataCreator = new TestDataCreator();
	}

	@After
	public void teardown() {
		Fixtures.deleteDatabase();
	}

	@Test
	public void listOfExperimentsReturned() {
		List<Map<String, String>> list = getExperimentsOwnedBy(username);
		assertEquals(1, list.size());
		Map<String, String> experiment = list.get(0);
		assertEquals("Primera prueba", experiment.get("name"));
	}

	private List<Map<String, String>> getExperimentsOwnedBy(String username) {
		Response response = GET("/experiment?username=" + username);
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
		List<ComparisonExperiment> list = ModelReadingUtils.getExperimentsOwnedBy(username);
		long experimentId = list.get(0).id;
		Map<String, String> experiment = getExperiment(experimentId);
		assertNotNull(experiment.get("id"));
		assertNotNull(experiment.get("name"));
		assertNotNull(experiment.get("probeMolecules"));
		assertNotNull(experiment.get("targetMolecules"));
		assertNotNull(experiment.get("engine"));
		assertNotNull(experiment.get("chargeType"));
		assertNotNull(experiment.get("ponderations"));
		assertNotNull(experiment.get("allAlignments"));
		assertNotNull(experiment.get("bestAlignments"));
		assertNotNull(experiment.get("status"));
	}

	@Test
	public void namesInScoringsDifferForMoleculeAndDeployment() {
		User owner = User.findByUserName(username);
		ComparisonExperiment experiment = dataCreator.getSmallEvaluatedExperiment(owner);
		Map<String, ?> experimentData = getExperiment(experiment.id);
		List<Map<String, String>> allAlignments = (List<Map<String, String>>) experimentData.get("allAlignments");
		List<Map<String, String>> bestAlignments = (List<Map<String, String>>) experimentData.get("bestAlignments");
		String deploymentName = getTargetName(allAlignments.get(0));
		String moleculeName = getTargetName(bestAlignments.get(0));
		assertTrue(deploymentName.contains("_"));
		assertFalse(moleculeName.contains("_"));
	}

	private String getTargetName(Map<String, ?> map) {
		Map<String, String> target = (Map<String, String>) map.get("target");
		return target.get("name");
	}

	private Map<String, String> getExperiment(long experimentId) {
		Response response = GET(String.format("/experiment/%d?username=%s", experimentId, username));
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
	public void deploymentPropertiesAreShownInJson() {
		ComparisonExperiment experiment = createFakeExperimentWithProperties();
		Map<String, ?> experimentData = getExperiment(experiment.id);
		List<Map<String, ?>> deploymentScorings = (List<Map<String, ?>>) experimentData.get("allAlignments");
		Map<String, String> probe = (Map<String, String>) deploymentScorings.get(0).get("probe");
		assertNotNull(probe.get("random"));
	}

	private ComparisonExperiment createFakeExperimentWithProperties() {
		User owner = User.findByUserName(username);
		ComparisonExperiment experiment = dataCreator.getSmallEvaluatedExperiment(owner);
		List<Deployment> deployments = experiment.probeMolecules.getAllDeployments();
		for (Deployment deployment : deployments) {
			deployment.putProperty("random", String.valueOf(Math.random() * 100));
			experiment.probeMolecules.save();
		}
		return experiment.save();
	}

	@Test
	public void getExperimentData() {
		List<ComparisonExperiment> list = ModelReadingUtils.getExperimentsOwnedBy(username);
		long experimentId = list.get(0).id;
		ModelForListing experiment = getExperimentData(experimentId, username);
		assertEquals("Primera prueba", experiment.name);
	}

	private ModelForListing getExperimentData(long experimentId, String username) {
		Response response = GET("/experiment/" + experimentId + "?username=" + username);
		assertIsOk(response);
		assertContentType("application/json", response);
		String content = getContent(response);
		Gson gson = new Gson();
		ModelForListing experiment = gson.fromJson(content, ModelForListing.class);
		return experiment;
	}

	@Test
	public void experimentIsAddedCorrectly() {
		List<MoleculeDatabase> databases = ModelReadingUtils.getMoleculeDatabasesOwnedBy(username);
		List<ModelForListing> engines = ModelReadingUtils.getListOfEngines();
		Map<String, String> experimentParams = new HashMap<String, String>();
		experimentParams.put("username", username);
		experimentParams.put("experimentName", "Tester");
		experimentParams.put("targetDatabaseId", "" + databases.get(0).id);
		experimentParams.put("probeDatabaseId", "" + databases.get(0).id);
		experimentParams.put("engineId", "" + engines.get(0).id);
		experimentParams.put("chargeType", Factory.GASTEIGER + "");
		Response response = POST("/experiment", experimentParams);
		assertIsOk(response);
		List<ComparisonExperiment> experiments = ModelReadingUtils.getExperimentsOwnedBy(username);
		assertEquals(2, experiments.size());
		long lastId = experiments.get(experiments.size() - 1).id;
		ComparisonExperiment experiment = ComparisonExperiment.findById(lastId);
		assertEquals("Tester", experiment.name);
		assertEquals(Factory.GASTEIGER, experiment.chargeType);
	}

	@Test
	public void cannotIntroduceExperimentWithUnownedParts() {
		List<MoleculeDatabase> databasesUser1 = ModelReadingUtils.getMoleculeDatabasesOwnedBy("xmaresma");
		List<MoleculeDatabase> databasesUser2 = ModelReadingUtils.getMoleculeDatabasesOwnedBy("xarroyo");
		List<ModelForListing> engines = ModelReadingUtils.getListOfEngines();
		Map<String, String> experimentParams = new HashMap<String, String>();
		experimentParams.put("username", username);
		experimentParams.put("experimentName", "Tester");
		experimentParams.put("targetDatabaseId", "" + databasesUser1.get(0).id);
		experimentParams.put("probeDatabaseId", "" + databasesUser2.get(0).id);
		experimentParams.put("engineId", "" + engines.get(0).id);
		POST("/experiment", experimentParams);
		// assertStatus(500, response);
		List<ComparisonExperiment> experiments = ModelReadingUtils.getExperimentsOwnedBy(username);
		assertEquals(1, experiments.size());
	}

	@Test
	public void cannotReadUnownedExperiment() {
		try {
			List<ComparisonExperiment> list = ModelReadingUtils.getExperimentsOwnedBy(username);
			Map<String, String> experimentParams = new HashMap<String, String>();
			GET("/experiment/" + list.get(0).id + "?username=" + experimentParams);
			fail("Should throw exception");
		} catch (Exception e) {
		}
	}

	// @Test
	// This cannot be tested because it is an asynchronous job
	public void removeExperiment() {
		List<ComparisonExperiment> ExperimentList = ModelReadingUtils.getExperimentsOwnedBy(username);
		long experimentId = ExperimentList.get(0).id;
		DELETE("/experiment/" + experimentId + "?username=" + username);
		ComparisonExperiment deletedExperiment = ComparisonExperiment.find("id = ?", experimentId).first();
		assertNull(deletedExperiment);
	}

	@Test
	public void rescoreExperiment() {
		ComparisonExperiment experiment = createFakeExperimentWithProperties();
		Ponderation ponderation = new Ponderation();
		ponderation.name = "prueba";
		ponderation.weights.A = 12;
		ponderation.owner = User.findByUserName(username);
		ponderation.save();
		Map<String, String> experimentParams = new HashMap<String, String>();
		experimentParams.put("username", username);
		experimentParams.put("ponderationId", "" + ponderation.id);
		POST("/experiment/" + experiment.id + "/rescore", experimentParams);
		experiment.refresh();
		assertEquals(2, experiment.getPonderations().size());
	}

	public void experimentIsCorrectlyRenamed() {

		User user = User.findByUserName("lnavarro");
		assertNotNull(user);
		ComparisonExperiment experiment = dataCreator.createExperiment();

		Long id = experiment.id;

		String newName = "new Experiment Name";

		Map<String, String> databaseParams = new HashMap<String, String>();

		databaseParams.put("username", user.username);
		databaseParams.put("name", newName);

		Response response = POST("/experiment/" + experiment.id + "/rename", databaseParams);
		assertIsOk(response);

		experiment = ComparisonExperiment.findById(id);
		assertEquals(newName, experiment.name);
	}

	@Test
	public void getGuestsReturnGuestsCorrectly() {

		User user = User.findByUserName("lnavarro");
		assertNotNull(user);
		ComparisonExperiment experiment = dataCreator.createExperiment();

		String[] guests = { "lnavarro", "xmaresma" };
		dataCreator.addGuestsToExperiment(experiment, guests);

		Long id = experiment.id;

		Map<String, String> databaseParams = new HashMap<String, String>();

		databaseParams.put("username", user.username);

		Response response = POST("/experiment/" + experiment.id + "/getGuests", databaseParams);
		assertIsOk(response);

		String guestsStr = getContent(response);

		String expected = "lnavarro,xmaresma,|" + dataCreator.getAllUsersNotInList();

		assertEquals(expected, guestsStr);
	}

	@Test
	public void setGuestsSetsGuestsCorrectly() {

		User user = User.findByUserName("aperreau");
		assertNotNull(user);
		ComparisonExperiment experiment = dataCreator.createExperiment();

		String[] guests = { "lnavarro", "xmaresma" };
		dataCreator.addGuestsToExperiment(experiment, guests);

		Long id = experiment.id;

		Map<String, String> databaseParams = new HashMap<String, String>();

		databaseParams.put("username", user.username);
		databaseParams.put("guests", "xmaresma,dbermudez");

		Response response = POST("/experiment/" + experiment.id + "/setGuests", databaseParams);
		assertIsOk(response);

		List<ComparisonExperimentGuest> comparisonExperimentGuests = ComparisonExperimentGuest
				.findByComparisonExperiment(experiment);
		assertEquals(2, comparisonExperimentGuests.size());
		assertEquals(comparisonExperimentGuests.get(1).username.username, "xmaresma");
		assertEquals(comparisonExperimentGuests.get(0).username.username, "dbermudez");
	}

	@Test
	public void changeOwnerCorrectly() {

		User user = User.findByUserName("aperreau");
		assertNotNull(user);
		ComparisonExperiment experiment = dataCreator.createExperiment();

		assertEquals("aperreau", experiment.owner.username);

		Long id = experiment.id;

		Map<String, String> databaseParams = new HashMap<String, String>();

		databaseParams.put("username", user.username);
		databaseParams.put("owner", "xmaresma");

		Response response = POST("/experiment/" + experiment.id + "/changeOwner", databaseParams);
		assertIsOk(response);

		List<ComparisonExperimentGuest> comparisonExperimentGuests = ComparisonExperimentGuest
				.findByComparisonExperiment(experiment);
		assertEquals(1, comparisonExperimentGuests.size());
		assertEquals(comparisonExperimentGuests.get(0).username.username, "aperreau");

		experiment = ComparisonExperiment.findAllOwnedBy((User) User.findByUserName("xmaresma")).get(0);
		assertNotNull(experiment);

		List<MoleculeDatabaseGuest> moleculeDatabaseGuests = MoleculeDatabaseGuest
				.findByMoleculeDatabase(experiment.probeMolecules);
		assertEquals(1, moleculeDatabaseGuests.size());
		assertEquals(moleculeDatabaseGuests.get(0).username.username, "xmaresma");

		moleculeDatabaseGuests.clear();
		moleculeDatabaseGuests = MoleculeDatabaseGuest.findByMoleculeDatabase(experiment.targetMolecules);
		assertEquals(1, moleculeDatabaseGuests.size());
		assertEquals(moleculeDatabaseGuests.get(0).username.username, "xmaresma");
	}

	@Test
	public void guestCannotDoModifyActionsOnExperiment() {
		User user = User.findByUserName("aperreau");
		assertNotNull(user);
		ComparisonExperiment experiment = dataCreator.createExperiment();
		String[] guests = { "lnavarro", "xmaresma" };
		dataCreator.addGuestsToExperiment(experiment, guests);

		Ponderation ponderation = new Ponderation();
		ponderation.name = "pruebaModify";
		ponderation.weights.A = 15;
		ponderation.owner = User.findByUserName(username);
		ponderation.save();

		Map<String, String> experimentParams = new HashMap<String, String>();
		experimentParams.put("ponderationId", "" + ponderation.id);
		experimentParams.put("username", "lnvarro");

		try {
			POST("/experiment/" + experiment.id + "/rescore", experimentParams); 
		} catch (Exception e) {
			if (!e.getMessage().contains("User tried to use an experiment it does not own")) {
				fail("Expected exception in guestCannotDoModifyActionsOnExperiment not found");
			}
		}
	}

	@Test
	public void guestCanDoReadActionsOnExperimentOthersNot() {
		User user = User.findByUserName("aperreau");
		assertNotNull(user);

		List<ComparisonExperiment> list = ModelReadingUtils.getExperimentsOwnedBy(username);
		ComparisonExperiment experiment = list.get(0);

		List<ComparisonExperimentGuest> comparisonExperimentGuests = ComparisonExperimentGuest
				.findByComparisonExperiment(experiment);
		String[] guests = { "lnavarro", "xmaresma" };
		dataCreator.addGuestsToExperiment(experiment, guests);

		Response response = GET("/experiment/" + experiment.id + "?username=lnavarro"); // OK
		assertIsOk(response);

		try {
			response = GET("/experiment/" + experiment.id + "?username=dbermudez"); // KO
		} catch (Exception e) {
			if (!e.getMessage().contains("User tried to use an experiment it does not own")) {
				fail("Expected exception in guestCanDoReadActionsOnExperiment not found");
			}
		}
	}

	@Test
	public void everybodyCanDoReadActionsOnCommonExperiment() {
		User user = User.findByUserName("aperreau");
		assertNotNull(user);

		List<ComparisonExperiment> list = ModelReadingUtils.getExperimentsOwnedBy(username);
		ComparisonExperiment experiment = list.get(0);

		Map<String, String> databaseParams = new HashMap<String, String>();

		databaseParams.put("username", "aperreau");
		databaseParams.put("owner", "common");

		Response response = POST("/experiment/" + experiment.id + "/changeOwner", databaseParams);
		assertIsOk(response);

		response = GET("/experiment/" + experiment.id + "?username=lnavarro"); 
		assertIsOk(response);
		response = GET("/experiment/" + experiment.id + "?username=dbermudez"); 
		assertIsOk(response);

	}

}