package engine.tridimensional.maps;


import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import engine.tridimensional.maps.SlurmTanimotoSimilarityCalculator;

import play.test.UnitTest;
import utils.TemplatedConfiguration;
import utils.scripts.ExternalScript;

public class SlurmTanimotoSimilarityCalculatorTest extends UnitTest {

	SlurmTanimotoSimilarityCalculator tanimoto;
	String script;
	
	@Before
	public void setUp() {
		tanimoto = new SlurmTanimotoSimilarityCalculator();
		script = TemplatedConfiguration.get("tanimoto");
	}
	
	@Test
	public void tanimotoExecutesCorrectScript() {
		String targetPath = "path1";
		String probePath = "path2";
		int similarityCalculationType = 0;
		int ncores = 1;
		
		ExternalScript launcher = EasyMock.createNiceMock(ExternalScript.class);
		String command = String.format(script, targetPath, probePath, similarityCalculationType, ncores);
		EasyMock.expect(launcher.paralelize(1, "tanimoto", command)).andReturn("A=23 B=0.2");
		
		EasyMock.replay(launcher);
		
		tanimoto.setLauncher(launcher);
		tanimoto.setBaseFileNames(targetPath, probePath);
		tanimoto.calculateTanimotos();
		
		EasyMock.verify(launcher);
	}


	@Test
	public void parsesOutputCorrectly() {
		String targetPath = "path1";
		String probePath = "path2";
		int similarityCalculationType = 1;
		int ncores = 1;
		
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		String command = String.format(script, targetPath, probePath, similarityCalculationType, ncores);
		EasyMock.expect(launcher.paralelize(1, "tanimoto", command)).andReturn("A=23 B=0.2");
		
		EasyMock.replay(launcher);
		
		tanimoto.setLauncher(launcher);
		tanimoto.setBaseFileNames(targetPath, probePath);
		tanimoto.setSimilarityCalculationType(similarityCalculationType);
		tanimoto.calculateTanimotos();
		
		assertEquals(23, tanimoto.getCalculatedTanimoto("A"), 0.0001);
		assertEquals(0.2, tanimoto.getCalculatedTanimoto("B"), 0.0001);
	}

	@Test
	public void parsesCompleteOutputCorrectly() {
		String staticPath = "path1";
		String dynamicPath = "path2";
		int similarityCalculationType = 0;
		int ncores = 1;
		
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		String command = String.format(script, staticPath, dynamicPath, similarityCalculationType, ncores);

		String slurmOutput = "A=0.0139435635186343 Br=0.00964807700799223 C=0.0131741254255687 Ca=0.00719690738608324 "+
		"Cl=0.0107028226187105 F=0.0177733854142156 Fe=0.0207901513724012 HD=0.0158124062968516 "+
		"I=0.00858351924288958 Mg=0.00735481893865877 Mn=0.10735481893865877 N=0.0119724507900141 "+
		"NA=0.0121536559321081 NS=0.0122615803814714 OA=0.0108383034119464 OS=0.0108383034119464 "+
		"P=0.0118735041940656 S=0.0114500073554651 SA=0.0120619557073195 Zn=0.0079451855091479 "+
		"d=0.106878463017894 e=0.0143217079962870";

		EasyMock.expect(launcher.paralelize(1, "tanimoto", command)).andReturn(slurmOutput);
		EasyMock.replay(launcher);
		
		tanimoto.setLauncher(launcher);
		tanimoto.setBaseFileNames(staticPath, dynamicPath);
		tanimoto.calculateTanimotos();
		
		assertEquals(0.013943, tanimoto.getCalculatedTanimoto("A"), 0.0001);
		assertEquals(0.009648, tanimoto.getCalculatedTanimoto("Br"), 0.0001);
		assertEquals(0.013174, tanimoto.getCalculatedTanimoto("C"), 0.0001);
		assertEquals(0.007196, tanimoto.getCalculatedTanimoto("Ca"), 0.0001);
		assertEquals(0.010702, tanimoto.getCalculatedTanimoto("Cl"), 0.0001);
		assertEquals(0.017773, tanimoto.getCalculatedTanimoto("F"), 0.0001);
		assertEquals(0.020790, tanimoto.getCalculatedTanimoto("Fe"), 0.0001);
		assertEquals(0.015812, tanimoto.getCalculatedTanimoto("HD"), 0.0001);
		assertEquals(0.008583, tanimoto.getCalculatedTanimoto("I"), 0.0001);
		assertEquals(0.007354, tanimoto.getCalculatedTanimoto("Mg"), 0.0001);
		assertEquals(0.107354, tanimoto.getCalculatedTanimoto("Mn"), 0.0001);
		assertEquals(0.011972, tanimoto.getCalculatedTanimoto("N"), 0.0001);
		assertEquals(0.012153, tanimoto.getCalculatedTanimoto("NA"), 0.0001);
		assertEquals(0.012261, tanimoto.getCalculatedTanimoto("NS"), 0.0001);
		assertEquals(0.010838, tanimoto.getCalculatedTanimoto("OA"), 0.0001);
		assertEquals(0.010838, tanimoto.getCalculatedTanimoto("OS"), 0.0001);
		assertEquals(0.011873, tanimoto.getCalculatedTanimoto("P"), 0.0001);
		assertEquals(0.011450, tanimoto.getCalculatedTanimoto("S"), 0.0001);
		assertEquals(0.012061, tanimoto.getCalculatedTanimoto("SA"), 0.0001);
		assertEquals(0.007945, tanimoto.getCalculatedTanimoto("Zn"), 0.0001);
		assertEquals(0.106878, tanimoto.getCalculatedTanimoto("d"), 0.0001);
		assertEquals(0.014321, tanimoto.getCalculatedTanimoto("e"), 0.0001);
	}

	@Test
	public void doesNotBreakWithExtraWhiteSpace() {
		String staticPath = "path1";
		String dynamicPath = "path2";
		String outputWithWhiteSpace = "A=23 B=0.2 \n";
		int similarityCalculationType = 0;
		int ncores = 1;
		
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		String command = String.format(script, staticPath, dynamicPath, similarityCalculationType, ncores);
		EasyMock.expect(launcher.paralelize(1, "tanimoto", command)).andReturn(outputWithWhiteSpace);
		
		EasyMock.replay(launcher);
		
		tanimoto.setLauncher(launcher);
		tanimoto.setBaseFileNames(staticPath, dynamicPath);
		tanimoto.calculateTanimotos();
		
		assertEquals(23, tanimoto.getCalculatedTanimoto("A"), 0.0001);
		assertEquals(0.2, tanimoto.getCalculatedTanimoto("B"), 0.0001);
	}

}
