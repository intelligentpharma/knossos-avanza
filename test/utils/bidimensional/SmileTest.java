package utils.bidimensional;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import play.test.UnitTest;
import utils.bidimensional.Fingerprint;
import utils.bidimensional.FingerprintFrequencyList;
import utils.bidimensional.Smile;

public class SmileTest extends UnitTest {

	@Test
	public void smileWithLessThan4AtomsHasNoLingos(){
		Smile smile = new Smile("C=C","mol1");
		Set<Fingerprint> lingos  = smile.getFrequencyList().getFingerprints();
		assertEquals(0, lingos.size());
	}

	@Test
	public void getLingosFromOneMoleculeCorrectly(){
		Smile smile = new Smile("C=C/c1ccc(c1cc","mol1");
		Set<Fingerprint> lingos  = smile.getFrequencyList().getFingerprints();
		assertEquals(10, lingos.size());
		assertTrue(lingos.contains(new Fingerprint("C=C/")));
		assertTrue(lingos.contains(new Fingerprint("=C/c")));
		assertTrue(lingos.contains(new Fingerprint("C/c0")));
		assertTrue(lingos.contains(new Fingerprint("/c0c")));
		assertTrue(lingos.contains(new Fingerprint("c0cc")));
		assertTrue(lingos.contains(new Fingerprint("0ccc")));
		assertTrue(lingos.contains(new Fingerprint("ccc(")));
		assertTrue(lingos.contains(new Fingerprint("cc(c")));
		assertTrue(lingos.contains(new Fingerprint("c(c0")));
		assertTrue(lingos.contains(new Fingerprint("(c0c")));
	}

	@Test
	public void getLingosFromOneDifficultMoleculeCorrectly(){
		Smile smile = new Smile("[O-]C(=C[N+]#N)CCC(C(=O)O)N","mol1");
		Set<Fingerprint> lingos  = smile.getFrequencyList().getFingerprints();
		assertEquals(24, lingos.size());
	}
	
	@Test
	public void getLingosFrequenciesFromOneMoleculeCorrectly(){
		Smile smile = new Smile("C=C/c1ccc(c1cc","mol1");
		FingerprintFrequencyList lingoFrecuencyList = smile.getFrequencyList();
		
		assertEquals(1,lingoFrecuencyList.getFingerprintFrequency(new Fingerprint("C=C/")));
		assertEquals(1,lingoFrecuencyList.getFingerprintFrequency(new Fingerprint("=C/c")));
		assertEquals(1,lingoFrecuencyList.getFingerprintFrequency(new Fingerprint("C/c0")));
		assertEquals(1,lingoFrecuencyList.getFingerprintFrequency(new Fingerprint("/c0c")));
		assertEquals(2,lingoFrecuencyList.getFingerprintFrequency(new Fingerprint("c0cc")));
		assertEquals(1,lingoFrecuencyList.getFingerprintFrequency(new Fingerprint("0ccc")));
		assertEquals(1,lingoFrecuencyList.getFingerprintFrequency(new Fingerprint("ccc(")));
		assertEquals(1,lingoFrecuencyList.getFingerprintFrequency(new Fingerprint("cc(c")));
		assertEquals(1,lingoFrecuencyList.getFingerprintFrequency(new Fingerprint("c(c0")));
		assertEquals(1,lingoFrecuencyList.getFingerprintFrequency(new Fingerprint("(c0c")));
	}

	@Test
	public void getLingosFrequenciesFromOneMoleculeWithUpperAndLowerCaseCorrectly(){
		Smile smile1 = new Smile("[O-][N+](=O)c1ccc(c(c1)[N+](=O)[O-])Cl","mol1");
		Smile smile2 = new Smile("[O-]C(=O)CC(C[N+](C)(C)C)OC(=O)C","mol2");
		FingerprintFrequencyList lingoFrecuencyList1 = smile1.getFrequencyList();
		FingerprintFrequencyList lingoFrecuencyList2 = smile2.getFrequencyList();
		
		assertEquals(1,lingoFrecuencyList1.getFingerprintFrequency(new Fingerprint("=O)c")));
		assertEquals(0,lingoFrecuencyList1.getFingerprintFrequency(new Fingerprint("=O)C")));
		assertEquals(0,lingoFrecuencyList2.getFingerprintFrequency(new Fingerprint("=O)c")));
		assertEquals(2,lingoFrecuencyList2.getFingerprintFrequency(new Fingerprint("=O)C")));
	}

	@Test
	public void getLingoWithCapitalAndLowerCaseInMoleculeFrequency(){
		Smile smile = new Smile("[O-]C(=O)CC(C[N+](C)(C)C)Oc(=O)C","mol1");
		FingerprintFrequencyList lingoFrecuencyList = smile.getFrequencyList();
			
		assertEquals(1,lingoFrecuencyList.getFingerprintFrequency(new Fingerprint("C(=O")));
		assertEquals(1,lingoFrecuencyList.getFingerprintFrequency(new Fingerprint("c(=O")));
	}
	
	@Test
	public void getLingoFrequencyOfNonPresentLingoReturnsZero(){
		Smile smile = new Smile("C=C/c1ccc(c1cc","mol1");
		FingerprintFrequencyList lingoFrecuencyList = smile.getFrequencyList();
		
		assertEquals(0,lingoFrecuencyList.getFingerprintFrequency(new Fingerprint("CC=/")));
	}

	@Test
	public void processASmile(){
		Smile smile = new Smile("CClBr1Cl1(cccccc)ClBr%32132456","mol1");
		String smileTextProcessed = "CLR0L0(cccccc)LR0";
		String smileTextCharBack = "CClBr0Cl0(cccccc)ClBr0";
		assertEquals(smileTextProcessed, smile.processSmile(smile.getCode()));
		assertEquals(smileTextCharBack, smile.getBackOriginalAtomNames(smileTextProcessed));
	}

	@Test
	public void smilesSimilarityToItselfIsTotal(){
		Smile smile = new Smile("C=C/c1ccc(c1cc","mol1");
		FingerprintFrequencyList lingoFrecuencyList = smile.getFrequencyList();
		smile.calculateSimilarity(lingoFrecuencyList, lingoFrecuencyList, lingoFrecuencyList.getFingerprints());
	}
	
	@Test
	public void comparingWithAnEmptyLingoSetThrowsException(){
		Smile smile = new Smile("C=C/c1ccc(c1cc","mol1");
		FingerprintFrequencyList lingoFrecuencyList = smile.getFrequencyList();
		assertEquals(-1.0, smile.calculateSimilarity(lingoFrecuencyList, lingoFrecuencyList, new HashSet<Fingerprint>()), 0.001);
		
		smile = new Smile("C","mol2");
		FingerprintFrequencyList emptylingoFrecuencyList = smile.getFrequencyList();
		assertEquals(-1.0, smile.calculateSimilarity(emptylingoFrecuencyList, lingoFrecuencyList, new HashSet<Fingerprint>()), 0.001);
		assertEquals(-1.0, smile.calculateSimilarity(lingoFrecuencyList, emptylingoFrecuencyList, new HashSet<Fingerprint>()), 0.001);
	}
	
	@Test
	public void calculateSmilesSimilarity(){
		Smile smile1 = new Smile("CHOCOHOLC","mol1");
		Smile smile2 = new Smile("CHOCOLOCO","mol2");
		
		FingerprintFrequencyList lingoFrecuencyList1 = smile1.getFrequencyList();
		FingerprintFrequencyList lingoFrecuencyList2 = smile2.getFrequencyList();
		
		Set<Fingerprint> smile1Lingos = lingoFrecuencyList1.getFingerprints();
		assertEquals(0.333, smile1.calculateSimilarity(lingoFrecuencyList1, lingoFrecuencyList2, smile1Lingos), 0.001);
	}
	
	@Test
	public void calculateSmilesSimilarityForASmileWithoutLingos(){
		Smile smile1 = new Smile("CHO","mol1");
		Smile smile2 = new Smile("CHOCOLOCO","mol2");
		
		FingerprintFrequencyList lingoFrecuencyList1 = smile1.getFrequencyList();
		FingerprintFrequencyList lingoFrecuencyList2 = smile2.getFrequencyList();
		
		Set<Fingerprint> smile1Lingos = lingoFrecuencyList1.getFingerprints();
		assertEquals(-1, smile1.calculateSimilarity(lingoFrecuencyList1, lingoFrecuencyList2, smile1Lingos), 0.001);
	}
	
	
	
	@Test
	public void storesNameCorrectly(){
		Smile smile1 = new Smile("CHOCOHOLC","mol1");
		assertEquals("mol1", smile1.getName());
	}
	
	@Test
	public void comparesIgnoringCase(){
		Smile smile1 = new Smile("COCHOCHHOoO", "mol1");
		Smile smile2 = new Smile("COCHOchHOOO", "mol1");
		Smile smile3 = new Smile("dOCHoCHhOOo", "mol3");
		assertEquals(0, smile1.compareTo(smile2));
		assertTrue(smile1.compareTo(smile3) < 0);		
	}
}
