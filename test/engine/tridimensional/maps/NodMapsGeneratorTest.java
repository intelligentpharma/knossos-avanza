package engine.tridimensional.maps;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.FileNotFoundException;

import junitx.framework.FileAssert;
import models.AlignmentBox;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import utils.scripts.ExternalScript;
import engine.factory.FileNameFactory;
import engine.tridimensional.maps.MapsGenerator;
import engine.tridimensional.maps.NodMapsGenerator;
import engine.tridimensional.maps.PdbqtRetriever;

public class NodMapsGeneratorTest extends UnitTest{

	NodMapsGenerator generator;
	
	AlignmentBox pdbBox;
	FileNameFactory fileNameFactory;
	PdbqtRetriever retriever;
	ExternalScript launcher;
	
	@Before
	public void setup() {
		generator = new NodMapsGenerator();
		generator.setMapType(MapsGenerator.INVERSE);
		generator.setAutogridCommand("autogrid42");

		pdbBox = new AlignmentBox(18.146, 8.188, 15.105, 148, 147, 141);
		 
		fileNameFactory = createMock(FileNameFactory.class);
		expect(fileNameFactory.getTargetGpf(MapsGenerator.INVERSE)).andReturn("tmp/file.gpf").anyTimes();
		expect(fileNameFactory.getTargetMapsPrefix(MapsGenerator.INVERSE)).andReturn("tmp/file.maps").anyTimes();
		expect(fileNameFactory.getTargetGlg(MapsGenerator.INVERSE)).andReturn("tmp/file.glg").anyTimes();
		
		retriever = createMock(PdbqtRetriever.class);
		expect(retriever.getPdbqt(fileNameFactory)).andReturn("test-files/nodMapsMolecule.pdbqt").anyTimes();

		launcher = createMock(ExternalScript.class);
		expect(launcher.paralelize("autogrid-inverse", "autogrid42 -p tmp/file.gpf -l tmp/file.glg")).andReturn("ok");

		generator.setFileNameFactory(fileNameFactory);
		generator.setPdbqtRetriever(retriever);
		generator.setLauncher(launcher);
	}

	@Test
	public void gpfFileIsCreatedCorrectly() throws FileNotFoundException {
		replay(fileNameFactory, retriever);
		
		generator.alignmentBox = pdbBox;
		generator.prepareGPF();
		
		File output = new File("tmp/file.gpf");		
		FileAssert.assertEquals(new File("test-files/nodMaps.gpf"), output);
		
		verify(fileNameFactory, retriever);
	}
	
	@Test
	public void generatesMapsUsingCorrectCommand(){
		
		replay(fileNameFactory, retriever, launcher);
		
		generator.generateMaps(pdbBox);
		
		verify(launcher);
	}
		
}