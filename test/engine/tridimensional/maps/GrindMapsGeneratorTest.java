package engine.tridimensional.maps;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import junitx.framework.FileAssert;
import models.AlignmentBox;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import utils.scripts.ExternalScript;
import engine.factory.FileNameFactory;
import engine.tridimensional.maps.GrindMapsGenerator;
import engine.tridimensional.maps.MapsGenerator;

public class GrindMapsGeneratorTest extends UnitTest {

	GrindMapsGenerator generator;
	
	@Before
	public void setup() {
		generator = new GrindMapsGenerator();
		generator.setMapType(MapsGenerator.STRAIGHT);
		generator.setAutogridCommand("autogrid");
	}
	
	public void returnGrindMaps(){
		List<String> grindMaps = generator.getGrindMaps();
		
		assertEquals("C", grindMaps.get(0));
		assertEquals("HD", grindMaps.get(1));
		assertEquals("OA", grindMaps.get(2));
	}
	
	@Test
	public void gpfFileIsCreatedCorrectly() throws FileNotFoundException {
		AlignmentBox pdbBox = new AlignmentBox(-1.847, -0.631, -0.210, 138, 126, 131);
		
		FileNameFactory fileNameFactory = createMock(FileNameFactory.class);
		expect(fileNameFactory.getTargetGpf(MapsGenerator.STRAIGHT)).andReturn("tmp/file.gpf").anyTimes();
		expect(fileNameFactory.getTargetMapsPrefix(MapsGenerator.STRAIGHT)).andReturn("tmp/file.maps").anyTimes();
			
		generator.setFileNameFactory(fileNameFactory);
		generator.setPdbqFileName("tmp/file.pdbqt");
		replay(fileNameFactory);
		
		generator.prepareGPF(pdbBox);
		File output = new File("tmp/file.gpf");
		
		FileAssert.assertEquals(new File("test-files/qsar/grinds/firstConformationWithoutSpherics.gpf"), output);
		
		verify(fileNameFactory);
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
