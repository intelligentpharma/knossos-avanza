package utils;

import java.util.List;

import models.Event;
import models.EventLevel;
import models.MoleculeDatabase;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.EventWriter;
import utils.experiment.TestDataCreator;

public class EventWriterTest extends UnitTest {

    private static final String HELLO_WORLD = "Hi World!";
	private static User user;
    private static MoleculeDatabase db;
    private static TestDataCreator testFactory;
    EventWriter eventWriter;
    

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        user = User.findByUserName("lnavarro");
        testFactory = new TestDataCreator();
        db = testFactory.createSmallDatabaseWithoutProperties(user);
        eventWriter = new EventWriter();
    }

        @Test
    public void addInfoEventCorrectly() {                    
        eventWriter.addInfoEvent(db, HELLO_WORLD);
        List<Event> list = Event.findAll();        
        Event event = list.get(0);
        assertEquals(HELLO_WORLD, event.message);
        assertEquals(EventLevel.INFO, event.level);
        assertEquals(false, event.seen);
        assertEquals(db.id, event.sourceId);
        assertEquals(db.getType(), event.sourceType);
//        assertEquals(user.username, event.username);        
    }

    @Test
    public void addErrorEventCorrectly() {        
    
        eventWriter.addErrorEvent(db, HELLO_WORLD);
        List<Event> list = Event.findAll();        
        Event event = list.get(0);
        assertEquals(HELLO_WORLD, event.message);
        assertEquals(EventLevel.ERROR, event.level);
        assertEquals(false, event.seen);
        assertEquals(db.id, event.sourceId);
        assertEquals(db.getType(), event.sourceType);
//        assertEquals(user.username, event.username);
    }

    @Test
    public void addSeenEventCorrectly() {        
    
        eventWriter.addSeenEvent(db, HELLO_WORLD);
        List<Event> list = Event.findAll();        
        Event event = list.get(0);
        assertEquals(HELLO_WORLD, event.message);
        assertEquals(EventLevel.INFO, event.level);
        assertEquals(true, event.seen);
        assertEquals(db.id, event.sourceId);
        assertEquals(db.getType(), event.sourceType);
//        assertEquals(user.username, event.username);
    }

    @Test
    public void addStatelessEventCorrectly() {        
    
        eventWriter.addStatelessParentEvent("moleculeDB", 666L, HELLO_WORLD, user.username);
        
        List<Event> list = Event.findAll();        
        Event event = list.get(0);
        assertEquals(HELLO_WORLD, event.message);
        assertEquals(EventLevel.ERROR, event.level);
        assertEquals(false, event.seen);
        assertEquals(666, event.sourceId, 0.001);
        assertEquals("moleculeDB", event.sourceType);
//        assertEquals(user.username, event.username);
    }
    
}
