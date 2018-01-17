package utils;

import java.util.ArrayList;
import java.util.List;

import org.junit.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import play.Logger;
import play.test.*;

public class TemplateTest extends UnitTest{
	
	@Test
	public void inlineTemplate(){
		ST hello = new ST("hello <name>");
		hello.add("name", "adrian");
		assertEquals("hello adrian", hello.render());
	}
	
	//@Test
	public void fileTemplate(){
		STGroupFile stGroup = new STGroupFile("test-files/template.stg");
		ST hello = stGroup.getInstanceOf("hello");
		hello.add("name", "adrian");
		assertEquals("hello adrian", hello.render());
	}
	
	//@Test
	public void fileMultiLineTemplate(){
		STGroupFile stGroup = new STGroupFile("test-files/template.stg");
		ST hello = stGroup.getInstanceOf("multilineHello");
		hello.add("name", "adrian");
		assertEquals("hello\nadrian", hello.render());
	}
	
	//@Test
	public void fileArrayIteratingTemplate(){
		STGroupFile stGroup = new STGroupFile("test-files/template.stg");
		ST hello = stGroup.getInstanceOf("arrayIteratingHello");
		List<String> names = new ArrayList<String>();
		names.add("adrian");
		names.add("joe");
		names.add("marie");
		hello.add("names", names);
		Logger.info("'%s'", hello.render());
		assertEquals("hello adrian\nhello joe\nhello marie", hello.render());		
	}
	
	//@Test
	public void sdfWrittenCorrectlyAsTemplate(){
		STGroupFile stGroup = new STGroupFile("templates/dpf.stg");
		ST sdf = stGroup.getInstanceOf("dpf");
		List<String> mapNames = new ArrayList<String>();
		mapNames.add("A");
		mapNames.add("C");
		mapNames.add("HD");
		mapNames.add("N");
		mapNames.add("OA");
		sdf.add("mapNames", mapNames);
		sdf.add("vdwMaps", "A C HD N OA");
		sdf.add("targetPathPrefix", "receptor/mol23.pdbqt");
		sdf.add("probePathPrefix", "ligand/mol23.pdbqt");
		sdf.add("X", 123);
		sdf.add("Y", 234);
		sdf.add("Z", 345);
		
		String sdfContents = sdf.render();
		Logger.info("\n%s" , sdfContents);
		
		assertEquals("hello adrian\rhello joe\rhello marie", sdfContents);		
	}

}
