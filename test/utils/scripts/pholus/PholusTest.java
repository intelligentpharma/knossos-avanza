package utils.scripts.pholus;

import java.util.ArrayList;

import models.ComparisonExperiment;
import models.Ponderation;

import org.easymock.EasyMock;
import org.junit.Test;

import utils.scripts.AbstractScriptLauncher;
import utils.scripts.ExternalScript;
import utils.scripts.ScriptTest;
import utils.scripts.pholus.PholusImpl;
import utils.scripts.pholus.PholusInput;
import utils.scripts.pholus.PholusOutput;

public class PholusTest extends ScriptTest {

	@Test
	public void scriptCalledCorrectly() {
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		String pholusRegex = "/usr/local/bin/R --vanilla --slave -f ./scripts/plsPholus.R --args .* clusterName";
		EasyMock.expect(launcher.paralelize(EasyMock.anyObject(String.class), EasyMock.matches(pholusRegex)))
			.andReturn("");

		PholusInput pholusInput = EasyMock.createNiceMock(PholusInput.class);
		PholusOutput pholusOutput = EasyMock.createNiceMock(PholusOutput.class);

		PholusImpl pholus = new PholusImpl("clusterName");
		pholus.launcher = launcher;

		EasyMock.replay(launcher, pholusInput, pholusOutput);

		pholus.setInputGenerator(pholusInput);
		pholus.setOutputParser(pholusOutput);
		pholus.calculate();

		EasyMock.verify(launcher, pholusInput, pholusOutput);
	}

	@Test
	public void getsPonderationsFromOutput() {
		PholusOutput pholusOutput = EasyMock.createNiceMock(PholusOutput.class);
		ArrayList<Ponderation> ponderations = new ArrayList<Ponderation>();
		EasyMock.expect(pholusOutput.getPonderations()).andReturn(ponderations);
		
		PholusImpl pholus = new PholusImpl("clusterName");

		EasyMock.replay(pholusOutput);
		
		pholus.setOutputParser(pholusOutput);
		assertEquals(ponderations, pholus.getPonderations());

		EasyMock.verify(pholusOutput );
	}

	@Override
	protected AbstractScriptLauncher getTestedObject() {
		PholusImpl pholus = new PholusImpl("clusterName");

		scriptInput = EasyMock.createNiceMock(PholusInput.class);
		scriptOutput = EasyMock.createNiceMock(PholusOutput.class);
		ComparisonExperiment experiment = new ComparisonExperiment();
		
		EasyMock.expect(((PholusInput)scriptInput).getExperiment()).andReturn(experiment).anyTimes();
		EasyMock.expect(((PholusInput)scriptInput).getName()).andReturn("").anyTimes();
		
		return pholus;
	}

	@Test
	public void createsCorrectConfigurationFile() {
		super.createsCorrectConfigurationFile();
	}

	@Test
	public void outputIsParsed() {
		super.outputIsParsed();
	}
	
	@Test
	public void executesScriptWithGivenCluster(){
		
	}
	
}
