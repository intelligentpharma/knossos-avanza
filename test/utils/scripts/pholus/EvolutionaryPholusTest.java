package utils.scripts.pholus;

import models.ComparisonExperiment;

import org.easymock.EasyMock;
import org.junit.Test;

import files.FileUtils;

import utils.scripts.AbstractScriptLauncher;
import utils.scripts.ExternalScript;
import utils.scripts.ScriptTest;
import utils.scripts.pholus.EvolutionaryPholus;
import utils.scripts.pholus.PholusInput;
import utils.scripts.pholus.PholusOutput;

public class EvolutionaryPholusTest extends ScriptTest {

	@Test
	public void scriptCalledCorrectly() {
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		String pholusRegex = "/usr/local/bin/R --vanilla --slave -f ./scripts/eaPholus.R --args .* clusterName";
		String ponderationText = "1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 " +
		"14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 1.2 1";
		EasyMock.expect(launcher.paralelize(EasyMock.anyObject(String.class), EasyMock.matches(pholusRegex)))
				.andReturn(ponderationText);

		FileUtils fileUtils = EasyMock.createNiceMock(FileUtils.class);
		
		PholusInput pholusData = EasyMock.createNiceMock(PholusInput.class);
		ComparisonExperiment experiment = new ComparisonExperiment();
		EasyMock.expect(pholusData.getExperiment()).andReturn(experiment);
		
		PholusOutput pholusOutput = EasyMock.createNiceMock(PholusOutput.class);

		EasyMock.replay(launcher, pholusData, pholusOutput, fileUtils);

		EvolutionaryPholus pholus = new EvolutionaryPholus("clusterName");
		pholus.launcher = launcher;
		pholus.setFileUtils(fileUtils);
		pholus.setInputGenerator(pholusData);
		pholus.setOutputParser(pholusOutput);
		pholus.calculate();

		EasyMock.verify(launcher, pholusData, pholusOutput, fileUtils);
	}

	@Test
	public void directoryCreated(){
		ExternalScript launcher = EasyMock.createNiceMock(ExternalScript.class);

		FileUtils fileUtils = EasyMock.createMock(FileUtils.class);
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.id = 12L;
		String directory = "./scripts/"+experiment.id +"_clusterName_pholus";
		fileUtils.createDirectory(directory);
		fileUtils.copyFile("./ext/bin/RealEA", directory);
		
		PholusInput pholusData = EasyMock.createNiceMock(PholusInput.class);
		EasyMock.expect(pholusData.getExperiment()).andReturn(experiment);
		EasyMock.expect(pholusData.getName()).andReturn("pholus");

		PholusOutput pholusOutput = EasyMock.createNiceMock(PholusOutput.class);

		EasyMock.replay(launcher, pholusData, pholusOutput, fileUtils);

		EvolutionaryPholus pholus = new EvolutionaryPholus("clusterName");
		pholus.launcher = launcher;
		pholus.setFileUtils(fileUtils);
		pholus.setInputGenerator(pholusData);
		pholus.setOutputParser(pholusOutput);
		pholus.calculate();

		EasyMock.verify(launcher, pholusData, pholusOutput, fileUtils);
	}

	@Override
	protected AbstractScriptLauncher getTestedObject() {
		EvolutionaryPholus pholus = new EvolutionaryPholus("clusterName");

		scriptInput = EasyMock.createNiceMock(PholusInput.class);
		scriptOutput = EasyMock.createNiceMock(PholusOutput.class);
		ComparisonExperiment experiment = new ComparisonExperiment();
		
		EasyMock.expect(((PholusInput)scriptInput).getExperiment()).andReturn(experiment).anyTimes();
		EasyMock.expect(((PholusInput)scriptInput).getName()).andReturn("").anyTimes();
		
		FileUtils fileUtils = EasyMock.createNiceMock(FileUtils.class);
		pholus.setFileUtils(fileUtils);
		
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
}