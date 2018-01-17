package models;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;

public class PhysicalMapsTest extends UnitTest {

	PhysicalMaps maps;
	
	@Before
    public void setup() {
		maps = new PhysicalMaps();
    }
    
    @Test
    public void setMapWorksCorrectly() {
    	maps.setMapValue(PhysicalMaps.aliphaticCarbon, 12.0);
    	assertEquals(12.0, maps.C, 0.01);
    }

	@Test
    public void getMapWorksCorrectly() {
    	maps.setMapValue("A", 12.0);
    	assertEquals(12.0, maps.getMapValue("A"), 0.0);
    }
    
    @Test
    public void fieldsReturnedCorrectly() {
    	Set<String> fieldNames = PhysicalMaps.getMapNames();
    	assertEquals(22, fieldNames.size());
    	assertTrue(fieldNames.contains("A"));
    	assertTrue(fieldNames.contains("e"));
    }
    
	@Test
	public void vdwNameMapsToString() {
		assertEquals("A Br C Ca Cl F Fe HD I Mg Mn N NA NS OA OS P S SA Zn ", PhysicalMaps.getVanDerWaalsInString());
	}
	
	@Test
	public void noMetalsInString(){
		assertEquals("A Br C Cl F HD I N NA NS OA OS P S SA ", PhysicalMaps.getNoMetalsInString());
	}

}
