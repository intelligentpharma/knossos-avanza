package controllers;

import org.junit.Test;

import play.mvc.Http.Response;
import play.test.FunctionalTest;
import utils.TemplatedConfiguration;

public class LogManagerTest extends FunctionalTest {

	@Test
	public void countUnseenEventsReturnsOkWithEvents() throws InterruptedException{
		Response response = POST("/logManager/666/view");
		assertIsOk(response);
		String content = getContent(response);
		
		String command = TemplatedConfiguration.get("tail")+" "+TemplatedConfiguration.get("logs_dir")+"/system.out";
		
		assertTrue(content.contains(command));
	}
}
