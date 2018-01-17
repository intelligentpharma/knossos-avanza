package utils.bidimensional;

import org.junit.Test;

import play.test.UnitTest;
import utils.bidimensional.Fingerprint;

public class LingoTest extends UnitTest {

	@Test
	public void lingosWithSameTextAreEqual(){
		Fingerprint lingo1 = new Fingerprint("COCO");
		Fingerprint lingo2 = new Fingerprint("COCO");
		assertTrue(lingo1.equals(lingo2));
		assertEquals(lingo1.hashCode(), lingo2.hashCode());
	}

	@Test
	public void lingosWithDifferentTextAreDifferent(){
		Fingerprint lingo1 = new Fingerprint("COCO");
		Fingerprint lingo2 = new Fingerprint("COCOCO");
		assertFalse(lingo1.equals(lingo2));
	}	
	
	@Test
	public void testDifferentBranchesForEquals(){
		Fingerprint lingo1 = new Fingerprint("COCO");
		Fingerprint lingo2 = null;
		assertFalse(lingo1.equals(lingo2));
		
		Object lingo3 = new Object();
		assertFalse(lingo1.equals(lingo3));

		lingo2 = new Fingerprint("COCOCO");
		assertFalse(lingo1.equals(lingo2));
	}
	
	
}
