package json;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class ListJsonConverterTest extends UnitTest {

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
    }
    
    @Test
    public void ListConvertedCorrectly(){
    	List<Object> data = new ArrayList<Object>();
    	Object item = new Object();
    	data.add(item);
    	data.add(item);
    	data.add(item);
    	ListJsonConverter converter = new ListJsonConverter();
    	JsonConverter itemConverter = EasyMock.createMock(JsonConverter.class);
    	itemConverter.setData(item);
    	EasyMock.expectLastCall().times(3);
    	EasyMock.expect(itemConverter.getJson()).andReturn("{\"name\":\"value\"}").times(3);
    	
    	EasyMock.replay(itemConverter);
    	
    	converter.setItemConverter(itemConverter);
    	converter.setData(data);
    	
    	String json = converter.getJson();
		ObjectMapper jsonParser = new ObjectMapper();
		List<Map<String, String>> parsedData = null;
		try {
			parsedData = jsonParser.readValue(json, List.class);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error in Json");
		}
		assertNotNull(parsedData);
		assertEquals("[{\"name\":\"value\"},{\"name\":\"value\"},{\"name\":\"value\"}]", json);
		assertEquals(3, parsedData.size());
		assertEquals("value", parsedData.get(0).get("name"));
		
		EasyMock.verify(itemConverter);
    }

}
