package files.formats.pdbqt;

import org.junit.Test;

import files.formats.pdbqt.PdbqtDataExtractorImpl;

import play.test.UnitTest;
import utils.scripts.ExternalScript;
import utils.scripts.ExternalScriptViaCommandLine;

public class PdbqtInfoTest extends UnitTest {

	@Test
	public void testExtractsPdbqtWithTwoModelsInfoCorrectly() {
		ExternalScript launcher = new ExternalScriptViaCommandLine();
		PdbqtDataExtractorImpl fileDataExtractor = new PdbqtDataExtractorImpl();
		fileDataExtractor.setLauncher(launcher);
		
		String input = "test-files/outputPdbqtTwoModel";
		fileDataExtractor.parseFile(input);

		assertEquals(-32.7, fileDataExtractor.getEnergy(), 0.01);
		assertEquals(0.00, fileDataExtractor.getEntropy(), 0.01);
	}
	
	@Test
	public void testExtractsPdbqtWithOneModelsInfoCorrectly() {
		ExternalScript launcher = new ExternalScriptViaCommandLine();
		PdbqtDataExtractorImpl fileDataExtractor = new PdbqtDataExtractorImpl();
		fileDataExtractor.setLauncher(launcher);
		
		String input = "test-files/outputPdbqtOneModel";
		fileDataExtractor.parseFile(input);

		assertEquals(-28.4, fileDataExtractor.getEnergy(), 0.01);
		assertEquals(0.00, fileDataExtractor.getEntropy(), 0.01);
	}	

}