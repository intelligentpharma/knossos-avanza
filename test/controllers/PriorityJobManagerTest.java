package controllers;

import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import utils.experiment.TestDataCreator;

public class PriorityJobManagerTest extends FunctionalTest {

	String username;
	TestDataCreator dataCreator;

	@Before
	public void setup() {
		username = "aperreau";
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		dataCreator = new TestDataCreator();
	}

	@Test
	public void aQueueWithoutItemsReturnsEmptyItemsJson() {
		Response response = GET("/priorityJobQueue");

		assertIsOk(response);
		assertContentType("application/json", response);
		String content = getContent(response);
		Logger.debug(content);

		String expected = new String(
				"{\"maxRunningJobs\": \"2\",\"maxSlurmJobs\": \"56\",\"runningJobs\": \"0\",\"unfinishedItems\": [ ],\"items\": [ ]}")
				.replace(" ", "").replace("\n", "").replace("\t", "");
		String actual = content.replace(" ", "").replace("\n", "").replace("\t", "");
		assertEquals(expected, actual);

	}

}