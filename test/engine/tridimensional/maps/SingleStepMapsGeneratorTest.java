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
import engine.tridimensional.maps.PdbqtRetriever;
import engine.tridimensional.maps.SingleStepMapsGenerator;

public class SingleStepMapsGeneratorTest extends UnitTest {

	SingleStepMapsGenerator generator;

	@Before
	public void setup() {
		generator = new SingleStepMapsGenerator();
		generator.setMapType(MapsGenerator.STRAIGHT);
		generator.setAutogridCommand("autogrid");
	}

	@Test
	public void gpfFileIsCreatedCorrectly() throws FileNotFoundException {
		AlignmentBox pdbBox = new AlignmentBox(-1.847, -0.631, -0.210, 138, 126, 131);
		
		FileNameFactory fileNameFactory = createMock(FileNameFactory.class);
		expect(fileNameFactory.getTargetGpf(MapsGenerator.STRAIGHT)).andReturn("tmp/file.gpf").anyTimes();
		expect(fileNameFactory.getTargetMapsPrefix(MapsGenerator.STRAIGHT)).andReturn("tmp/file.maps").anyTimes();
		
		PdbqtRetriever retriever = createMock(PdbqtRetriever.class);
		expect(retriever.getPdbqt(fileNameFactory)).andReturn("tmp/file.pdbqt");
		
		generator.setFileNameFactory(fileNameFactory);
		generator.setPdbqtRetriever(retriever);
		replay(fileNameFactory, retriever);
		
		generator.prepareGPF(pdbBox);
		File output = new File("tmp/file.gpf");
		FileAssert.assertEquals(new File("test-files/firstConformation.gpf"), output);
		
		verify(fileNameFactory, retriever);
	}
	

	@Test
	public void mapsAreGenerated() {
		
		AlignmentBox pdbBox = new AlignmentBox(-1.847, -0.631, -0.210, 138, 126, 131);

		String type = MapsGenerator.STRAIGHT;
		FileNameFactory fileNameFactory = createMock(FileNameFactory.class);
		
		expect(fileNameFactory.getTargetGpf(type)).andReturn("tmp/file.gpf").anyTimes();
		expect(fileNameFactory.getTargetGlg(type)).andReturn("tmp/file.glg").anyTimes();
		expect(fileNameFactory.getTargetMapsPrefix(type)).andReturn("type").anyTimes();
		
		generator.setFileNameFactory(fileNameFactory);
		
		ExternalScript launcher = createMock(ExternalScript.class);
		String command = "autogrid -p tmp/file.gpf -l tmp/file.glg";
		
		expect(launcher.paralelize("autogrid-"+type, command)).andReturn("").once();		
		replay(launcher, fileNameFactory);
		
		generator.setLauncher(launcher);
		generator.setMapType(type);
		generator.generateMaps(pdbBox);
		
		verify(launcher, fileNameFactory);
	}
}
