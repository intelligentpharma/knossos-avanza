package models;

import org.junit.Test;

import play.test.UnitTest;

public class MoleculePairTest extends UnitTest {

	@Test
	public void pairIsNotEqualToOtherClass(){
		MoleculePair pair = new MoleculePair(null, null);
		assertNotSame(pair, "hello");
	}
	
	@Test
	public void emptyPairEqualsItself(){
		MoleculePair pair = new MoleculePair(null, null);
		assertTrue(pair.equals(pair));
	}
	
	@Test
	public void pairWithoutProbeFieldEqualsItself(){
		MoleculePair pair = new MoleculePair(new Molecule(), null);
		assertTrue(pair.equals(pair));
	}

	@Test
	public void pairWithoutTargetFieldEqualsItself(){
		MoleculePair pair = new MoleculePair(null, new Molecule());
		assertTrue(pair.equals(pair));
	}
	
	@Test
	public void pairWithUnsavedMoleculesEqualsItself(){
		MoleculePair pair = new MoleculePair(new Molecule(), new Molecule());
		assertTrue(pair.equals(pair));
	}

	@Test
	public void pairsWithDifferentMoleculesAreNotEqual(){
		Molecule probe1 = new Molecule();
		probe1.id = 1L;
		Molecule target1 = new Molecule();
		target1.id = 1L;
		Molecule probe2 = new Molecule();
		probe2.id = 1L;
		Molecule target2 = new Molecule();
		target2.id = 2L;
		MoleculePair pair1 = new MoleculePair(target1, probe1);
		MoleculePair pair2 = new MoleculePair(target2, probe2);
		assertFalse(pair1.equals(pair2));
	}

	@Test
	public void pairsWithSameMoleculeIdsAreEqual(){
		Molecule probe1 = new Molecule();
		probe1.id = 1L;
		Molecule target1 = new Molecule();
		target1.id = 2L;
		Molecule probe2 = new Molecule();
		probe2.id = 1L;
		Molecule target2 = new Molecule();
		target2.id = 2L;
		MoleculePair pair1 = new MoleculePair(target1, probe1);
		MoleculePair pair2 = new MoleculePair(target2, probe2);
		assertTrue(pair1.equals(pair2));
	}

	@Test
	public void pairsThatAreEqualHaveSameHash(){
		Molecule probe1 = new Molecule();
		probe1.id = 1L;
		Molecule target1 = new Molecule();
		target1.id = 2L;
		Molecule probe2 = new Molecule();
		probe2.id = 1L;
		Molecule target2 = new Molecule();
		target2.id = 2L;
		MoleculePair pair1 = new MoleculePair(target1, probe1);
		MoleculePair pair2 = new MoleculePair(target2, probe2);
		assertEquals(pair1.hashCode(), pair2.hashCode());
		target1.name = "yo";
		target2.name = target1.name;
		probe1.name = "tu";
		probe2.name = probe1.name;
		assertEquals(pair1.hashCode(), pair2.hashCode());
	}
}
