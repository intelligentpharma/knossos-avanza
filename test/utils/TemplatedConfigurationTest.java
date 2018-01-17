package utils;

import java.util.List;

import org.junit.Test;
import org.stringtemplate.v4.ST;

import play.Play;
import play.test.UnitTest;
import utils.TemplatedConfiguration;

public class TemplatedConfigurationTest extends UnitTest{
	
	@Test
	public void templateFromApplicationConf(){
		String experimentDirTemplate = (String) Play.configuration.get("tmp.dir");
		assertEquals("<knossos_home>/tmp", experimentDirTemplate);
		String homePath = (String) Play.configuration.get("knossos_home");
		assertEquals(".", homePath);
		ST template = new ST(experimentDirTemplate);
		template.add("knossos_home", homePath);
		assertEquals("./tmp", template.render());
	}
	
	@Test
	public void getSimpleAttribute() {
		assertEquals(".", TemplatedConfiguration.get("knossos_home"));
	}
	
	@Test
	public void getTemplatedAttibute() {
		String tmpDir = TemplatedConfiguration.get("tmp.dir");
		assertEquals("./tmp",tmpDir);
	}
	
	@Test
	public void getRecursivelyTemplatedAttribute() {
		String pdbbox = TemplatedConfiguration.get("vmdScript");
		assertEquals("./scripts/vmd_cmd", pdbbox);
	}
	
	@Test
	public void singleAttributeIsFound() {
		List<String> attributes = TemplatedConfiguration.getVariableNamesFromTemplate("hello<knossos_path>dear");
		assertEquals("knossos_path", attributes.get(0));
	}
	
	@Test
	public void multiAttributesAreFound(){
		List<String> attributes = TemplatedConfiguration.getVariableNamesFromTemplate("hello<knossos_path>dear<tmp_dir>john");
		assertEquals("knossos_path", attributes.get(0));
		assertEquals("tmp_dir", attributes.get(1));
	}
	
	@Test
	public void noAttributesAreFound(){
		List<String> attributes = TemplatedConfiguration.getVariableNamesFromTemplate("hello john");
		assertEquals(0, attributes.size());
	}
	
	@Test
	public void noAttributesAreFoundForNullKey(){
		List<String> attributes = TemplatedConfiguration.getVariableNamesFromTemplate(null);
		assertEquals(0, attributes.size());
	}
}
