
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

public class ApplicationTest extends FunctionalTest {

	@Before
	public void setup() {
		Fixtures.deleteDatabase();		
		Fixtures.loadModels("data.yml");
	}

	@After
	public void teardown() {
		Fixtures.deleteDatabase();
	}

	@Test
	public void testValidationWorks() {
		Map<String, String> loginUserParams = new HashMap<String, String>();
		loginUserParams.put("username", "aperreau");
		loginUserParams.put("password", "adrian");
		Response response = POST("/authenticate", loginUserParams);
		assertIsOk(response);
		assertContentType("application/json", response);
		assertCharset("utf-8", response);
		assertEquals("true", getContent(response));
	}

}
