package engine.tridimensional.maps;

import org.junit.Test;

import engine.tridimensional.maps.NoActionMapsGenerator;

import play.test.UnitTest;

public class NoActionMapsGeneratorTest extends UnitTest{

	@Test(expected = UnsupportedOperationException.class)
	public void getMapsPrefixShouldNotBeCalled() {
		NoActionMapsGenerator generator = new NoActionMapsGenerator();
		generator.getMapsPrefix();
	}
	
	@Test
	public void generateMapsDoesNothing(){
		NoActionMapsGenerator generator = new NoActionMapsGenerator();
		generator.generateMaps(null);
	}
		
}

