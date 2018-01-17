package utils.scripts;

import java.io.File;

import junitx.framework.FileAssert;

import org.easymock.EasyMock;

import play.test.UnitTest;
import utils.scripts.AbstractScriptLauncher;
import utils.scripts.ExternalScript;
import utils.scripts.ScriptInput;
import utils.scripts.ScriptOutput;

public abstract class ScriptTest extends UnitTest {

	protected ScriptInput scriptInput;
	protected ScriptOutput scriptOutput;
	
	public void createsCorrectConfigurationFile() {
		AbstractScriptLauncher script = getTestedObject();
		ExternalScript launcher = EasyMock.createNiceMock(ExternalScript.class);
		EasyMock.expect(scriptInput.getInputData()).andReturn("pholus Data");		

		
		
		script.launcher = launcher;

		EasyMock.replay(launcher, scriptInput, scriptOutput);

		script.setInputGenerator(scriptInput);
		script.setOutputParser(scriptOutput);
		script.calculate();
		File dataFile = script.getDataFile();

		FileAssert.assertEquals(new File("test-files/plsPholus.txt"), dataFile);
		EasyMock.verify(launcher, scriptInput, scriptOutput);
	}

	protected abstract AbstractScriptLauncher getTestedObject();

	public void outputIsParsed() {
		AbstractScriptLauncher pholus = getTestedObject();

		ExternalScript launcher = EasyMock.createNiceMock(ExternalScript.class);
		String ponderationText = "1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 " +
		"14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 1.2 1";
		EasyMock.expect(launcher.paralelize(EasyMock.anyObject(String.class), EasyMock.anyObject(String.class))).
			andReturn(ponderationText).anyTimes();
		EasyMock.expect(launcher.launch(EasyMock.anyObject(String.class))).
			andReturn(ponderationText).anyTimes();

		scriptOutput.parse(ponderationText);
		pholus.launcher = launcher;

		EasyMock.replay(launcher, scriptInput, scriptOutput);
		
		pholus.setInputGenerator(scriptInput);
		pholus.setOutputParser(scriptOutput);
		pholus.calculate();

		EasyMock.verify(launcher, scriptInput, scriptOutput );
	}

}
