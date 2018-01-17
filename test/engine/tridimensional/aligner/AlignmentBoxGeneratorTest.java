package engine.tridimensional.aligner;

import models.AlignmentBox;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import engine.tridimensional.aligner.AlignmentBoxCalculatorImpl;

import play.test.UnitTest;
import utils.TemplatedConfiguration;
import utils.scripts.ExternalScript;

public class AlignmentBoxGeneratorTest extends UnitTest {

	AlignmentBoxCalculatorImpl parser;
	
	static String pythonScript;
	
	@Before
	public void setup() {
		pythonScript = TemplatedConfiguration.get("pythonsh");
		parser = new AlignmentBoxCalculatorImpl();
	}

	@Test
	public void referenceInfoParsing() {
		ExternalScript launcher = EasyMock.createMock(ExternalScript.class);
		EasyMock.expect(launcher.launch(EasyMock.matches("awk -f ./scripts/pdbbox_fixed_offset.awk add=6 file.pdbqt"))).andReturn("1 2 3 4 5 6").once();
		EasyMock.replay(launcher);
		parser.setLauncher(launcher);
		
		parser.calculateAlignmentBox("file.pdbqt");
		AlignmentBox info = parser.getAlignmentBox();
		assertEquals(1, info.centerX, 0.0001);
		assertEquals(2, info.centerY, 0.0001);
		assertEquals(3, info.centerZ, 0.0001);
		assertEquals(4, info.sizeX, 0.0001);
		assertEquals(5, info.sizeY, 0.0001);
		assertEquals(6, info.sizeZ, 0.0001);
	}

	
}
