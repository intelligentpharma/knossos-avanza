package engine.tridimensional.aligner;

import models.AlignmentBox;

import static org.easymock.EasyMock.*;
import org.junit.Test;

import engine.tridimensional.aligner.AlignmentBoxCalculator;
import engine.tridimensional.aligner.AlignmentBoxCalculatorImpl;

import play.test.UnitTest;
import utils.scripts.ExternalScript;


public class AlignmentBoxCalculatorTest extends UnitTest {

	@Test
	public void defaultBoxSetCorrectly() {
		AlignmentBoxCalculator calculator = new AlignmentBoxCalculatorImpl();
		AlignmentBox box = new AlignmentBox();
		calculator.setDefaultAlignmentBox(box);
		
		assertEquals(box, calculator.getAlignmentBox());
	}
	
	@Test
	public void boxCalculatedCorrectly() {
		AlignmentBoxCalculatorImpl calculator = new AlignmentBoxCalculatorImpl();
		ExternalScript launcher = createMock(ExternalScript.class);
		expect(launcher.launch("awk -f ./scripts/pdbbox_fixed_offset.awk add=6 somepath")).andReturn("1 2 3 4 5 6");
		calculator.setLauncher(launcher);
		
		replay(launcher);
		
		calculator.calculateAlignmentBox("somepath");
		AlignmentBox box = calculator.getAlignmentBox();
		
		assertEquals(1, box.centerX, 0.01);
		assertEquals(2, box.centerY, 0.01);
		assertEquals(3, box.centerZ, 0.01);
		assertEquals(4, box.sizeX);
		assertEquals(5, box.sizeY);
		assertEquals(6, box.sizeZ);
		
		verify(launcher);
	}
}
