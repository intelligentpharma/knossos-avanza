package engine.tridimensional.maps;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.TreeSet;


import org.junit.Test;

import engine.tridimensional.maps.MapsNameExtractor;
import engine.tridimensional.maps.MapsNameExtractorImpl;

import play.test.UnitTest;

public class MapsNameExtractorTest extends UnitTest {
	
	@Test
	public void extractCorrectMapsNamesFromPdbqt() throws FileNotFoundException{
		File pdbqtFile = new File("test-files/1hnw_prodock_easy.pdbqt");
		Set<String> mapsNames = new TreeSet<String>();
		Set<String> expectedMapsNames = new TreeSet<String>();
		expectedMapsNames.add("N");
		expectedMapsNames.add("HD");
		expectedMapsNames.add("C");
		expectedMapsNames.add("Cl");
		expectedMapsNames.add("A");
		expectedMapsNames.add("Mg");
		MapsNameExtractor extractor = new MapsNameExtractorImpl();
		mapsNames = extractor.getMapsNamesFromPdbqtFile(pdbqtFile);
		
		assertEquals(expectedMapsNames, mapsNames);
	}

}
