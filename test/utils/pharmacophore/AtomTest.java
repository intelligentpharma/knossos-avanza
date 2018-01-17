package utils.pharmacophore;

import org.junit.Test;

import play.test.UnitTest;
import utils.pharmacophore.Atom;

public class AtomTest extends UnitTest{

	@Test
	public void getAtomType(){
		Atom atom1 = new Atom(2.35, -5.65, 56,"C");
		assertEquals("C", atom1.getAtomType());
	}
	
	@Test
	public void calculateDistances(){
		Atom atom1 = new Atom(2.35, -5.65, 56,"C");
		Atom atom2 = new Atom(1.587, 7.105, -8,"HD");
		
		double distance = atom1.calculateDistanceWith(atom2);
		
		assertEquals(65.2631, distance, 0.001);
	}
	
}
