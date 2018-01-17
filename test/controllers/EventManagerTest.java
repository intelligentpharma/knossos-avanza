package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.MoleculeDatabase;
import models.User;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import play.Logger;
import play.mvc.Http.Response;
import play.mvc.Scope.Session;
import play.test.Fixtures;
import play.test.FunctionalTest;
import utils.experiment.TestDataCreator;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EventManagerTest extends FunctionalTest {

    private static User user;
    private static MoleculeDatabase db;
    private static MoleculeDatabase db2;
    private static TestDataCreator testFactory;
    private static Map<String, String> databaseParams = new HashMap<String, String>();

    @BeforeClass
    public static void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        String username = "dbermudez";
        databaseParams.put("username", username);
        user = User.findByUserName("dbermudez");
        Session.current().put("username", user.username);
        testFactory = new TestDataCreator();
        db = testFactory.createSmallDatabaseWithoutProperties(user);
        db2 = testFactory.createSmallDatabaseWithoutProperties(user);
    }
        
    

    @AfterClass
    public static void teardown() {
        Fixtures.deleteDatabase();
    }

    @Test
    public void countUnseenEventsReturnsOkWithNoEvents() {
    	cleanUnseenEvents();
        String content = getNumberOfUnseenEvents();
        assertEquals("0,0,0", content);
    }

    @Test
    public void countUnseenEventsReturnsOkWithErrorEvents() throws InterruptedException {
    	cleanUnseenEvents();
        db.addErrorEvent("Test Test Test");
        db.addErrorEvent("Test2 Test2 Test2");

        String content = getNumberOfUnseenEvents();

        assertEquals("0,2,0", content);
    }
    
    @Test
    public void countUnseenEventsReturnsOkWithInfoEvents() throws InterruptedException {
    	cleanUnseenEvents();
        db.addInfoEvent("Test Test Test");
        db.addInfoEvent("Test2 Test2 Test2");

        String content = getNumberOfUnseenEvents();

        assertEquals("2,0,0", content);
    }
    
    @Test
    public void countUnseenEventsReturnsOkWithWarningEvents() throws InterruptedException {
    	cleanUnseenEvents();
        db.addWarningEvent("Test Test Test");

        String content = getNumberOfUnseenEvents();

        assertEquals("0,0,1", content);
    }

    @Test
    public void listEventsReturnsOkForAllEvents() {
    	setup();
    	
        db.addInfoEvent("Test3 Test3 Test3");
        db2.addInfoEvent("Test3 Test3 Test3");
        db.addWarningEvent("Test3 Test3 Test3");
        db.addErrorEvent("Test3 Test3 Test3");

        Response response = GET("/event?username=dbermudez");
        assertIsOk(response);
        assertContentType("application/json", response);

        String content = getContent(response);
        Logger.info(content);

        ObjectMapper jsonParser = new ObjectMapper();

        List<Map<String, String>> events = null;
        try {
            events = jsonParser.readValue(content, List.class);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error in Json");
        }

        assertEquals(4, events.size());
    }

    @Test
    public void markEventsAsSeenWorksOk() {
       
        cleanUnseenEvents();

        String content = getNumberOfUnseenEvents();

        assertEquals("0,0,0", content);
    }

	private void cleanUnseenEvents() {
		Response response = POST("/event/666/markEventsAsSeen", databaseParams);
        assertIsOk(response);
	}

    private String getNumberOfUnseenEvents() {
        Response response = POST("/event/666/countUnseenEvents", databaseParams);        
        String content = getContent(response);
        return content;
    }

    @Test
    public void listEventsReturnsOkForAGivenSourceId() {
    	setup();
        db2.addInfoEvent("Test DB2");

        Response response = GET("/event?username=dbermudez&sourceId=" + db2.id);
        assertIsOk(response);
        assertContentType("application/json", response);

        String content = getContent(response);

        ObjectMapper jsonParser = new ObjectMapper();

        List<Map<String, String>> events = null;
        try {
            events = jsonParser.readValue(content, List.class);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error in Json");
        }

        assertEquals(1, events.size());
    }
}
