package files.formats.other;

import org.easymock.EasyMock;
import org.junit.Test;

import files.formats.other.DlgDataExtractorImpl;

import play.test.UnitTest;
import utils.TemplatedConfiguration;
import utils.scripts.ExternalScript;
import utils.scripts.ExternalScriptViaCommandLine;

public class DlgInfoTest extends UnitTest {

	@Test
	public void testExtractsDlgInfoCorrectly() {
		ExternalScript launcher = new ExternalScriptViaCommandLine();
		DlgDataExtractorImpl fileDataExtractor = new DlgDataExtractorImpl();
		fileDataExtractor.setLauncher(launcher);
		
		String input = "test-files/DlgTest.dlg";
		fileDataExtractor.parseFile(input);

		assertEquals(0.0, fileDataExtractor.getEntropy(), 0.01);
		assertEquals(-55.76,fileDataExtractor.getEnergy(), 0.01);
	}
	
	@Test
	public void testSimpleDlgInfoCommandIsCorrect(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		String converter = TemplatedConfiguration.get("dlgInfo");
		String convertCommand = String.format(converter, "dlgName");
		EasyMock.expect(launcher.launch(convertCommand)).andReturn("12.00 -13.9 13 14 15").once();
		
		EasyMock.replay(launcher);
		
		DlgDataExtractorImpl fileDataExtractor = new DlgDataExtractorImpl();
		fileDataExtractor.setLauncher(launcher);
		fileDataExtractor.parseFile("dlgName");
		
		assertEquals(12, fileDataExtractor.getEntropy(), 0.01);
		assertEquals(-13.9, fileDataExtractor.getEnergy(), 0.01);
		assertEquals(13, fileDataExtractor.getEnergyMedian(), 0.01);
		assertEquals(14, fileDataExtractor.getEnergyBigC(), 0.01);
		assertEquals(15, fileDataExtractor.getEnergyBigCMedian(), 0.01);
		
		EasyMock.verify(launcher);
	}	

	@Test
	public void testComplexDlgInfoCommandIsCorrect(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		String converter = TemplatedConfiguration.get("dlgInfo");
		String convertCommand = String.format(converter, "dlgName");
		EasyMock.expect(launcher.launch(convertCommand)).andReturn("+2.55e+07 -3.55e+07 -4.55e+07 -5.55e+07 -6.55e+07").once();

		EasyMock.replay(launcher);
		
		DlgDataExtractorImpl fileDataExtractor = new DlgDataExtractorImpl();
		fileDataExtractor.setLauncher(launcher);
		fileDataExtractor.parseFile("dlgName");
		
		assertEquals(new Double("+2.55e+07"),fileDataExtractor.getEntropy());
		assertEquals(new Double("-3.55e+07"), fileDataExtractor.getEnergy());
		assertEquals(new Double("-4.55e+07"), fileDataExtractor.getEnergyMedian(), 0.01);
		assertEquals(new Double("-5.55e+07"), fileDataExtractor.getEnergyBigC(), 0.01);
		assertEquals(new Double("-6.55e+07"), fileDataExtractor.getEnergyBigCMedian(), 0.01);

		EasyMock.verify(launcher);
	}	
	
}
