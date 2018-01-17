package utils.grinds;

import models.Distance;
import models.GrindPoint;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import utils.grinds.DistanceComparator;

public class DistanceComparatorTest extends UnitTest{
	
	DistanceComparator comparator;
	
	@Before
	public void setup(){
		comparator = new DistanceComparator();
	}	
	
	@Test
	public void compareDistance(){
		GrindPoint grindPoint1 = new GrindPoint(1, 2.3, "hola", 1L,1);
		GrindPoint grindPoint2 = new GrindPoint(2, 3.3, "adeu", 1L,1);
		Distance distance1 = new Distance(grindPoint1, grindPoint2, 0.2);
		Distance distance2 = new Distance(grindPoint1, grindPoint2, 0.9);
		Distance distance3 = new Distance(grindPoint1, grindPoint2, 0.2);
		
		assertEquals(-1, comparator.compare(distance1,distance2));
		assertEquals(1, comparator.compare(distance2,distance1));
		assertEquals(0, comparator.compare(distance1,distance3));
		
	}

}
