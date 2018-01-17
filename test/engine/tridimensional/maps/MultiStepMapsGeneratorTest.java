package engine.tridimensional.maps;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.TreeSet;

import junitx.framework.FileAssert;
import models.AlignmentBox;

import org.junit.Before;
import org.junit.Test;

import engine.factory.FileNameFactory;
import engine.tridimensional.maps.MapsGenerator;
import engine.tridimensional.maps.MultiStepMapsGenerator;
import engine.tridimensional.maps.PdbqtRetriever;

import play.test.UnitTest;
import utils.scripts.ExternalScript;

public class MultiStepMapsGeneratorTest extends UnitTest{

	MultiStepMapsGenerator generator;
	
	@Before
	public void setup() {
		generator = new MultiStepMapsGenerator();
		generator.setMapType(MapsGenerator.STRAIGHT);
		generator.setAutogridCommand("autogrid42");
	}

	@Test
	public void gpfFileIsCreatedCorrectly() throws FileNotFoundException {
		AlignmentBox pdbBox = new AlignmentBox(-1.847, -0.631, -0.210, 138, 126, 131);
		
		FileNameFactory fileNameFactory = createMock(FileNameFactory.class);
		expect(fileNameFactory.getTargetGpf(MapsGenerator.STRAIGHT)).andReturn("tmp/file.gpf").anyTimes();
		expect(fileNameFactory.getTargetMapsPrefix(MapsGenerator.STRAIGHT)).andReturn("tmp/file.maps").anyTimes();
		
		PdbqtRetriever retriever = createMock(PdbqtRetriever.class);
		expect(retriever.getPdbqt(fileNameFactory)).andReturn("test-files/1hnw_prodock_easy.pdbqt").anyTimes();
		
		generator.setFileNameFactory(fileNameFactory);
		generator.setPdbqtRetriever(retriever);
		generator.alignmentBox = pdbBox;
		replay(fileNameFactory, retriever);
		
		Set<String> probeNames = new TreeSet<String>();
		probeNames.add("A");
		probeNames.add("Br");
		generator.prepareGPFWithSelectedMaps(probeNames);
		File output = new File("tmp/file.gpf");
		FileAssert.assertEquals(new File("test-files/firstConformationWithSomeMaps.gpf"), output);
		
		verify(fileNameFactory, retriever);
	}
	
	@Test
	public void inverseMapsAreGenerated() {
		
		AlignmentBox pdbBox = new AlignmentBox(-1.847, -0.631, -0.210, 138, 126, 131);

		String type = MapsGenerator.INVERSE;
		FileNameFactory fileNameFactory = createMock(FileNameFactory.class);
		expect(fileNameFactory.getTargetGpf(type)).andReturn("tmp/file.gpf").anyTimes();
		expect(fileNameFactory.getTargetGlg(type)).andReturn("tmp/file.glg").anyTimes();
		expect(fileNameFactory.getTargetMapsPrefix(type)).andReturn(type).anyTimes();
		generator.setFileNameFactory(fileNameFactory);
		
		ExternalScript launcher = createMock(ExternalScript.class);
		String command = "autogrid42 -p tmp/file.gpf -l tmp/file.glg";		
		expect(launcher.paralelize("autogrid-"+type, command)).andReturn("").anyTimes();		
		generator.setLauncher(launcher);

		replay(launcher, fileNameFactory);

		generator.setMapType(type);
		generator.generateMaps(pdbBox);
		
		verify(launcher, fileNameFactory);
	}	

	@Test
	public void outputMapsAreGenerated() {
		
		AlignmentBox pdbBox = new AlignmentBox(-1.847, -0.631, -0.210, 138, 126, 131);

		String type = MapsGenerator.OUTPUT;
		FileNameFactory fileNameFactory = createMock(FileNameFactory.class);
		expect(fileNameFactory.getAlignmentGpf(type)).andReturn("tmp/file.gpf").anyTimes();
		expect(fileNameFactory.getAlignmentGlg(type)).andReturn("tmp/file.glg").anyTimes();
		expect(fileNameFactory.getAlignmentMapsPrefix(type)).andReturn(type).anyTimes();
		generator.setFileNameFactory(fileNameFactory);
		
		ExternalScript launcher = createMock(ExternalScript.class);
		String command = "autogrid42 -p tmp/file.gpf -l tmp/file.glg";		
		expect(launcher.paralelize("autogrid-"+type, command)).andReturn("").anyTimes();		
		generator.setLauncher(launcher);

		replay(launcher, fileNameFactory);

		generator.setMapType(type);
		generator.generateMaps(pdbBox);
		
		verify(launcher, fileNameFactory);
	}	
	
}
