package json;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class MapJsonConverterTest extends UnitTest {

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
    }
    
    @Test
    public void MapConvertedCorrectly(){
    	Map<String, String> data = new HashMap<String, String>();
    	String key1 = "key1";
    	String key2 = "key2";
    	String value = "value";
    	data.put(key1, value);
    	data.put(key2, value);
    	MapJsonConverter converter = new MapJsonConverter();
    	JsonConverter keyConverter = EasyMock.createMock(JsonConverter.class);
    	JsonConverter valueConverter = EasyMock.createMock(JsonConverter.class);
    	keyConverter.setData(key1);
    	keyConverter.setData(key2);
    	valueConverter.setData(value);
    	EasyMock.expectLastCall().times(2);
    	
    	EasyMock.expect(keyConverter.getJson()).andReturn(key1).times(2);
    	EasyMock.expect(valueConverter.getJson()).andReturn(value).times(2);

    	EasyMock.replay(keyConverter, valueConverter);
    	
    	converter.setKeyConverter(keyConverter);
    	converter.setValueConverter(valueConverter);
    	converter.setData(data);
    	
    	String json = converter.getJson();
		assertEquals("{key1:value,key1:value}", json);
		
		EasyMock.verify(keyConverter, valueConverter);
    }

}
