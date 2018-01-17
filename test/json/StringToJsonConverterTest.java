package json;

import org.junit.Test;

import play.test.UnitTest;

public class StringToJsonConverterTest extends UnitTest {
	
	@Test
	public void stringConvertedCorrectly(){
		StringToJsonConverter converter = new StringToJsonConverter();
		converter.setData("hello");
		assertEquals("\"hello\"", converter.getJson());
	}
}
