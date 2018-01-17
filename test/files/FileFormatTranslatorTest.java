package files;

import java.io.File;

import junitx.framework.FileAssert;

import org.easymock.EasyMock;
import org.junit.Test;

import files.FileFormatTranslatorImpl;
import files.FileUtils;
import files.FileUtilsImpl;

import play.test.UnitTest;
import utils.Factory;
import utils.TemplatedConfiguration;
import utils.scripts.ExternalScript;
import utils.scripts.ExternalScriptViaCommandLine;

public class FileFormatTranslatorTest extends UnitTest {

	@Test
	public void testCreatesPdbqtCorrectly() {
		ExternalScript launcher = new ExternalScriptViaCommandLine();
		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);
		
		String input = "test-files/DlgTest.dlg";
		String output = "tmp/DlgTest_out.pdbqt";
		String expectedOutput = "test-files/DlgTest_out.pdbqt";

		translator.convertDlgToPdbqt(input, output);
		
		FileAssert.assertEquals(new File(expectedOutput), new File(output));
	}
	
	@Test
	public void testDlgToPdbqtCommandIsCorrect(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		String converter = TemplatedConfiguration.get("dlgToPdbqtConverter");
		String convertCommand = String.format(converter, "dlgName","pdbqtName");
		EasyMock.expect(launcher.launch(convertCommand)).andReturn("").once();
		
		EasyMock.replay(launcher);
		
		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);
		translator.convertDlgToPdbqt("dlgName", "pdbqtName");
		EasyMock.verify(launcher);
	}

	@Test
	public void testConversionPdbqtToSdfIsCorrect() {
		ExternalScript launcher = new ExternalScriptViaCommandLine();
		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);

		String input = "test-files/test2.pdbqt";
		String expectedOutput = "test-files/test2.sdf";
		String output = "tmp/test2.sdf";		

		translator.convertPdbqtToSdf(input, output);
		FileUtils fileUtils = new FileUtilsImpl();
		fileUtils.removeAllOccurrencesOfLineFromFile(output, output, "OpenBabel", false);
		FileAssert.assertEquals(new File(expectedOutput), new File(output));
	}	

	@Test
	public void testSdfToStaticPdbqtOriginalCommands(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		EasyMock.expect(launcher.launch("/usr/local/bin/obabel -isdf file.sdf  -omol2 -Ofile.sdf.mol2")).andReturn("").once();
		EasyMock.expect(launcher.launch("pythonsh ./scripts/prepare_receptor4.py -r file.sdf.mol2 -A none -o file.pdbqt")).andReturn("").once();
		//EasyMock.expect(launcher.launch("/usr/local/bin/obabel -ipdbqt file.pdbqt  -xr -opdbqt -Ofile.pdbqt")).andReturn("").once();

		EasyMock.replay(launcher);

		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);
		translator.convertSdfToStaticPdbqt("file.sdf", "file.pdbqt", Factory.ORIGINAL);

		EasyMock.verify(launcher);
	}

	@Test
	public void testSdfToStaticPdbqtGasteigerCommands(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		EasyMock.expect(launcher.launch("/usr/local/bin/obabel -isdf file.sdf  -xr -opdbqt -Ofile.pdbqt")).andReturn("").once();

		EasyMock.replay(launcher);

		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);
		translator.convertSdfToStaticPdbqt("file.sdf", "file.pdbqt", Factory.GASTEIGER);

		EasyMock.verify(launcher);
	}

	@Test
	public void testSdfToStaticPdbqtMMFF94Commands(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		EasyMock.expect(launcher.launch("/usr/local/bin/obabel -isdf file.sdf --partialcharge mmff94 -xr -opdbqt -Ofile.pdbqt")).andReturn("").once();

		EasyMock.replay(launcher);

		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);
		translator.convertSdfToStaticPdbqt("file.sdf", "file.pdbqt", Factory.MMFF94);

		EasyMock.verify(launcher);
	}

	@Test
	public void testSdfToStaticPdbqtEEMCommands(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		EasyMock.expect(launcher.launch("/usr/local/bin/obabel -isdf file.sdf --partialcharge eem -xr -opdbqt -Ofile.pdbqt")).andReturn("").once();

		EasyMock.replay(launcher);

		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);
		translator.convertSdfToStaticPdbqt("file.sdf", "file.pdbqt", Factory.EEM);

		EasyMock.verify(launcher);
	}

	@Test
	public void testSdfToDynamicPdbqtOriginalCommands(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		EasyMock.expect(launcher.launch("/usr/local/bin/obabel -isdf file.sdf  -omol2 -Ofile.sdf.mol2")).andReturn("").once();
		EasyMock.expect(launcher.launch("pythonsh ./scripts/prepare_ligand4.py -l file.sdf.mol2 -o file.pdbqt")).andReturn("").once();
		//EasyMock.expect(launcher.launch("/usr/local/bin/obabel -ipdbqt file.pdbqt  -opdbqt -Ofile.pdbqt")).andReturn("").once();

		EasyMock.replay(launcher);

		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);
		translator.convertSdfToDynamicPdbqt("file.sdf", "file.pdbqt", Factory.ORIGINAL);

		EasyMock.verify(launcher);
	}

	@Test
	public void testSdfToDynamicPdbqtGasteigerCommands(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		EasyMock.expect(launcher.launch("/usr/local/bin/obabel -isdf file.sdf  -opdbqt -Ofile.pdbqt")).andReturn("").once();

		EasyMock.replay(launcher);

		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);
		translator.convertSdfToDynamicPdbqt("file.sdf", "file.pdbqt", Factory.GASTEIGER);

		EasyMock.verify(launcher);
	}

	@Test
	public void testSdfToDynamicPdbqtMMFF94Commands(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		EasyMock.expect(launcher.launch("/usr/local/bin/obabel -isdf file.sdf --partialcharge mmff94 -opdbqt -Ofile.pdbqt")).andReturn("").once();

		EasyMock.replay(launcher);

		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);
		translator.convertSdfToDynamicPdbqt("file.sdf", "file.pdbqt", Factory.MMFF94);

		EasyMock.verify(launcher);
	}
	
	@Test
	public void testConversionSmiToSmiCanonizedIsCorrect() {
		ExternalScript launcher = new ExternalScriptViaCommandLine();
		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);

		String input = "test-files/uncanonical.smi";
		String expectedOutput = "test-files/uncanonicalCanonized.smi";
		String output = "tmp/uncanonical.smi";

		translator.convertSmiToCanonizedSmi(input, output);
		FileAssert.assertEquals(new File(expectedOutput), new File(output));
	}
	
	@Test
	public void testConvertSmiToSmiWithoutCounterIonsIsCorrect(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		EasyMock.expect(launcher.launch("/usr/local/bin/obabel -ismi input.smi -r -e -ocan -Ooutput.smi")).andReturn("");

		EasyMock.replay(launcher);

		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);

		translator.convertSmiToSmiWithoutCounterIons("input.smi", "output.smi");

		EasyMock.verify(launcher);
	}
	
	@Test
	public void testCorinaCommandsAreCorrect1(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		
		EasyMock.expect(launcher.launch("./ext/bin/corina -i t=sdf -o t=sdf,lname -d rs,neu,rc,mc=10,de=20,flapn,sc,stergen,msc=8,msi=300,preserve,names,wh,r2d input.sdf output.sdf")).andReturn("");

		EasyMock.replay(launcher);

		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);

		translator.generateConformations("input.sdf", "output.sdf", true, true, true, "10", "20", true, true, true, "8", "300", true, true, true, true);

		EasyMock.verify(launcher);
	}
	
	@Test
	public void testCorinaCommandsAreCorrect2(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		
		EasyMock.expect(launcher.launch("./ext/bin/corina -i t=sdf -o t=sdf,lname -d neu,rc,mc=10,de=20,flapn,sc,stergen,msc=8,msi=300,preserve,names,wh,r2d input.sdf output.sdf")).andReturn("");

		EasyMock.replay(launcher);

		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);

		translator.generateConformations("input.sdf", "output.sdf", false, true, true, "10", "20", true, true, true, "8", "300", true, true, true, true);

		EasyMock.verify(launcher);
	}
	
	@Test
	public void testCorinaCommandsAreCorrect3(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		
		EasyMock.expect(launcher.launch("./ext/bin/corina -i t=sdf -o t=sdf,lname -d rc,mc=10,de=20,flapn,sc,stergen,msc=8,msi=300,preserve,names,wh,r2d input.sdf output.sdf")).andReturn("");

		EasyMock.replay(launcher);

		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);

		translator.generateConformations("input.sdf", "output.sdf", false, false, true, "10", "20", true, true, true, "8", "300", true, true, true, true);

		EasyMock.verify(launcher);
	}
	@Test
	public void testCorinaCommandsAreCorrect4(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		
		EasyMock.expect(launcher.launch("./ext/bin/corina -i t=sdf -o t=sdf,lname -d stergen,msc=8,msi=300,preserve,names,wh,r2d input.sdf output.sdf")).andReturn("");

		EasyMock.replay(launcher);

		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);

		translator.generateConformations("input.sdf", "output.sdf", false, false, false, "10", "20", true, true, true, "8", "300", true, true, true, true);

		EasyMock.verify(launcher);
	}
	@Test
	public void testCorinaCommandsAreCorrect5(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		
		EasyMock.expect(launcher.launch("./ext/bin/corina -i t=sdf -o t=sdf,lname -d names,wh,r2d input.sdf output.sdf")).andReturn("");

		EasyMock.replay(launcher);

		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);

		translator.generateConformations("input.sdf", "output.sdf", false, false, false, "10", "20", true, true, false, "8", "300", true, true, true, true);

		EasyMock.verify(launcher);
	}
	@Test
	public void testCorinaCommandsAreCorrect6(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		
		EasyMock.expect(launcher.launch("./ext/bin/corina -i t=sdf -o t=sdf,lname -d wh,r2d input.sdf output.sdf")).andReturn("");

		EasyMock.replay(launcher);

		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);

		translator.generateConformations("input.sdf", "output.sdf", false, false, false, "10", "20", true, true, false, "8", "300", true, false, true, true);

		EasyMock.verify(launcher);
	}
	@Test
	public void testCorinaCommandsAreCorrect7(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		
		EasyMock.expect(launcher.launch("./ext/bin/corina -i t=sdf -o t=sdf,lname -d r2d input.sdf output.sdf")).andReturn("");

		EasyMock.replay(launcher);

		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);

		translator.generateConformations("input.sdf", "output.sdf", false, false, false, "10", "20", false, false, false, "8", "300", false, false, false, true);

		EasyMock.verify(launcher);
	}
	@Test
	public void testCorinaCommandsAreCorrect8(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		
		EasyMock.expect(launcher.launch("./ext/bin/corina -i t=sdf -o t=sdf,lname input.sdf output.sdf")).andReturn("");

		EasyMock.replay(launcher);

		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);

		translator.generateConformations("input.sdf", "output.sdf", false, false, false, "10", "20", false, false, false, "8", "300", false, false, false, false);

		EasyMock.verify(launcher);
	}
	@Test
	public void testCorinaCommandsAreCorrect9(){
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		
		EasyMock.expect(launcher.launch("./ext/bin/corina -i t=sdf -o t=sdf,lname -d rs,neu,rc,mc=1,de=2,flapn,sc,stergen,msc=3,msi=4,preserve,names,wh,r2d input.sdf output.sdf")).andReturn("");

		EasyMock.replay(launcher);

		FileFormatTranslatorImpl translator = new FileFormatTranslatorImpl();
		translator.setLauncher(launcher);

		translator.generateConformations("input.sdf", "output.sdf", true, true, true, "1", "2", true, true, true, "3", "4", true, true, true, true);

		EasyMock.verify(launcher);
	}





}
