package files;

import java.io.File;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import files.DeploymentPropertyExtractorImpl;
import files.FileUtils;

import play.test.UnitTest;
import utils.scripts.ExternalScript;

public class DeploymentPropertyExtractorTest extends UnitTest{

	DeploymentPropertyExtractorImpl extractor;
	ExternalScript script;
	
	String molProps1 = "name             MCMC0000004_t001_i001_c001\n"+
						"formula          C5H12ClN+\n"+
						"mol_weight       232.235\n"+
						"exact_mass       232.654\n"+
						"$$$$\n";
	
	String molProps2 = "name             MCMC0000005_t001_i001_c001\n"+
						"formula          C6H4ClF+\n"+
						"mol_weight       334.327\n"+
						"exact_mass       334.687\n"+
						"$$$$\n";
	
	String molProps3 = "name             MCMC00000019_t001_i001_c002\n"+
						"formula          C23H30NO3+\n"+
						"mol_weight       334.327\n"+
						"exact_mass       335.234\n"+
						"$$$$\n";
	
	@Before
	public void setup(){
		extractor = new DeploymentPropertyExtractorImpl();
		FileUtils fileUtils = EasyMock.createMock(FileUtils.class);
		EasyMock.expect(fileUtils.isSdf(EasyMock.anyObject(String.class))).andReturn(true);

		script = EasyMock.createMock(ExternalScript.class);
		File file = new File("file.sdf");
		String cmd = "/usr/local/bin/obprop "+ file.getAbsolutePath();
		EasyMock.expect(script.launchAndIgnoreErrorsWithProcessBuilder(cmd)).andReturn(molProps1+molProps2+molProps3);
		EasyMock.replay(fileUtils, script);
		
		extractor.setFileUtils(fileUtils);
		extractor.setExternalLauncher(script);
		extractor.parse(file);
	}

	@Test
	public void launchesExternalScript(){
		EasyMock.verify(script);
	}

	@Test
	public void extractsFirstMoleculeFromSdfCorrectly(){
		//String molName = "MCMC0000004_t001_i001_c001";
		assertEquals("232.235", extractor.getMolecularWeight(Long.valueOf(1)));
		assertEquals(1, extractor.getNpol(Long.valueOf(1)));
		assertEquals(7, extractor.getNhea(Long.valueOf(1)));
	}

	@Test
	public void extractsSecondMoleculeFromSdfCorrectly(){
		//String molName = "MCMC0000005_t001_i001_c001";
		assertEquals("334.327", extractor.getMolecularWeight(Long.valueOf(2)));
		assertEquals(0, extractor.getNpol(Long.valueOf(2)));
		assertEquals(8, extractor.getNhea(Long.valueOf(2)));
	}

	@Test
	public void extractsLastMoleculeFromSdfCorrectly(){
		//String molName = "MCMC00000019_t001_i001_c002";
		assertEquals("334.327", extractor.getMolecularWeight(Long.valueOf(3)));
		assertEquals(4, extractor.getNpol(Long.valueOf(3)));
		assertEquals(27, extractor.getNhea(Long.valueOf(3)));
	}
	
	@Test
	public void sdfFileWithDifferentExtensionGetsParsed(){
		extractor = new DeploymentPropertyExtractorImpl();
		String originalFileName = "sdfwithdifferentExtension";
		File originalFile = new File(originalFileName);
		File tmpFile = new File("/tmp/"+originalFileName+".sdf");
		FileUtils fileUtils = EasyMock.createMock(FileUtils.class);
		EasyMock.expect(fileUtils.isSdf(originalFile.getAbsolutePath())).andReturn(false);
		EasyMock.expect(fileUtils.getFileNameWithoutExtension(originalFile)).andReturn(originalFileName);
		EasyMock.expect(fileUtils.copyToTmpAs(originalFile, originalFileName+".sdf")).andReturn(tmpFile);

		script = EasyMock.createMock(ExternalScript.class);
		String cmd = "/usr/local/bin/obprop "+ EasyMock.matches(".*sdf");
		EasyMock.expect(script.launchAndIgnoreErrorsWithProcessBuilder(cmd)).andReturn(molProps1+molProps2+molProps3);
		EasyMock.replay(script, fileUtils);
		
		extractor.setExternalLauncher(script);
		extractor.setFileUtils(fileUtils);
		extractor.parse(originalFile);
	}
}
