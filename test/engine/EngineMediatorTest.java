package engine;

import static org.easymock.EasyMock.*;
import models.AlignmentBox;
import models.ComparisonExperiment;

import org.junit.Test;

import engine.AbstractEngine;
import engine.factory.FileNameFactory;
import engine.tridimensional.aligner.AlignmentBoxCalculator;

import play.test.UnitTest;

public abstract class EngineMediatorTest extends UnitTest {
	
	protected abstract AbstractEngine getEngine();
	
	@Test
	public void getProbeBox(){
		AbstractEngine engine = getEngine();
		AlignmentBoxCalculator calculator = createMock(AlignmentBoxCalculator.class);
		AlignmentBox box = new AlignmentBox();
		expect(calculator.getAlignmentBox()).andReturn(box);
		engine.setProbeBoxCalculator(calculator);
		
		replay(calculator);
		
		assertEquals(box, engine.getProbeBox());

		verify(calculator);
	}

	@Test
	public void getTargetBox(){
		AbstractEngine engine = getEngine();
		AlignmentBoxCalculator calculator = createMock(AlignmentBoxCalculator.class);
		AlignmentBox box = new AlignmentBox();
		expect(calculator.getAlignmentBox()).andReturn(box);
		engine.setTargetBoxCalculator(calculator);
		engine.experiment = new ComparisonExperiment();
		
		replay(calculator);
		
		assertEquals(box, engine.getTargetBox());
		
		verify(calculator);
	}

	@Test
	public void getDpf(){
		AbstractEngine engine = getEngine();
		FileNameFactory factory = createMock(FileNameFactory.class);
		expect(factory.getDpf()).andReturn("dpf");
		engine.setFileNameFactory(factory);
		
		replay(factory);
		
		assertEquals("dpf", engine.getDpf());
	}

	@Test
	public void getProbePdbqt(){
		AbstractEngine engine = getEngine();
		FileNameFactory factory = createMock(FileNameFactory.class);
		expect(factory.getProbePdbqt()).andReturn("pdbqt");
		engine.setFileNameFactory(factory);
		
		replay(factory);
		
		assertEquals("pdbqt", engine.getProbePdbqt());
	}

	@Test
	public void getTargetPdbqt(){
		AbstractEngine engine = getEngine();
		FileNameFactory factory = createMock(FileNameFactory.class);
		expect(factory.getTargetPdbqt()).andReturn("pdbqt");
		engine.setFileNameFactory(factory);
		
		replay(factory);
		
		assertEquals("pdbqt", engine.getTargetPdbqt());
	}

	@Test
	public void getOutputPdbqt(){
		AbstractEngine engine = getEngine();
		FileNameFactory factory = createMock(FileNameFactory.class);
		expect(factory.getOutputPdbqt()).andReturn("pdbqt");
		engine.setFileNameFactory(factory);
		
		replay(factory);
		
		assertEquals("pdbqt", engine.getOutputPdbqt());
	}

}
