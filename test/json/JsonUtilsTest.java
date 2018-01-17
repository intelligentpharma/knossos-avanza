package json;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import play.test.UnitTest;

public class JsonUtilsTest extends UnitTest {

	@Test
	public void newLinesEscaped(){
		assertEquals("\\n", JsonUtils.escapeForJson("\n"));
		assertEquals("Something\\nelse", JsonUtils.escapeForJson("Something\nelse"));
	}
	
	@Test
	public void doubleQuotesEscaped(){
		assertEquals("\\\"", JsonUtils.escapeForJson("\""));
		assertEquals("Something\\\"else", JsonUtils.escapeForJson("Something\"else"));
	}
	
	@Test
	public void doesNotBreakWhenEscapingNullStrings(){
		assertNull(JsonUtils.escapeForJson((String)null));
	}
	
	@Test
	public void escapesAllInlist(){
		List<String> input = new ArrayList<String>();
		input.add("hola");
		input.add("adios\"");
		input.add("hola\nadios");
		
		List<String> output = JsonUtils.escapeForJson(input);
		assertEquals(3, output.size());
		assertEquals("hola", output.get(0));
		assertEquals("adios\\\"", output.get(1));
		assertEquals("hola\\nadios", output.get(2));
	}
}
