package json;

import models.ModelForListing;

import org.junit.Test;

import play.test.UnitTest;

public class ModelForListingToJsonConverterTest extends UnitTest {
	
	@Test
	public void modelForListingConvertedCorrectly(){
		ModelForListing model = new ModelForListing(1, "test");
		
		ModelForListingToJsonConverter converter = new ModelForListingToJsonConverter();
		converter.setData(model);
		assertEquals("{\"id\":\"1\",\"name\":\"test\"}", converter.getJson());
	}
}