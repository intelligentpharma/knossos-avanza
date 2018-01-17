package models;
import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class AuthenticationTest extends UnitTest {

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
    }
    
    @Test
    public void tryConnectAsUser() {
        // Create a new user and save it
        new User("Bob", "bob@gmail.com", "secret").save();
        
        // Test 
        assertNotNull(User.connect("Bob", "secret"));
        assertNull(User.connect("Bob", "badpassword"));
        assertNull(User.connect("Tom", "secret"));
    }

    @Test
    public void userValidationWorks() {
    	Fixtures.loadModels("data.yml");

    	User user = User.connect("aperreau", "adrian");
    	assertEquals("aperreau", user.username);
    	assertEquals("adrian", user.password);
    	assertEquals("aperreau@intelligentpharma.com", user.email);
    }
    
}
