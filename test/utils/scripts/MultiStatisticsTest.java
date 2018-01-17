package utils.scripts;

import org.easymock.EasyMock;
import org.junit.Test;

import utils.scripts.AbstractScriptLauncher;
import utils.scripts.ExternalScript;
import utils.scripts.MultiStatistics;
import utils.scripts.MultiStatisticsOutput;
import utils.scripts.ScriptInput;
import utils.scripts.ScriptOutput;

public class MultiStatisticsTest extends ScriptTest {

	@Test
	public void scriptCalledCorrectly() {
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		String pholusRegex = "/usr/local/bin/R --vanilla --slave -f ./scripts/ExperimentsValidation.R --args .*";
		EasyMock.expect(launcher.launch(EasyMock.matches(pholusRegex)))
			.andReturn("");

		ScriptInput input = EasyMock.createNiceMock(ScriptInput.class);
		ScriptOutput output = EasyMock.createNiceMock(ScriptOutput.class);

		MultiStatistics multi = new MultiStatistics();
		multi.launcher = launcher;

		EasyMock.replay(launcher, input, output);

		multi.setInputGenerator(input);
		multi.setOutputParser(output);
		multi.calculate();

		EasyMock.verify(launcher, input, output);
	}

	@Test
	public void returnsOutput() {
		ScriptOutput pholusOutput = new MultiStatisticsOutput();
		
		MultiStatistics multi = new MultiStatistics();

		multi.setOutputParser(pholusOutput);
		assertEquals(pholusOutput, multi.getOutput());

	}

	@Override
	protected AbstractScriptLauncher getTestedObject() {
		MultiStatistics multiStatistics = new MultiStatistics();

		scriptInput = EasyMock.createNiceMock(ScriptInput.class);
		scriptOutput = EasyMock.createNiceMock(ScriptOutput.class);
		
		multiStatistics.setInputGenerator(scriptInput);
		multiStatistics.setOutputParser(scriptOutput);

		return multiStatistics;
	}

	@Test
	public void createsCorrectConfigurationFile() {
		super.createsCorrectConfigurationFile();
	}

	@Test
	public void outputIsParsed() {
		super.outputIsParsed();
	}
}