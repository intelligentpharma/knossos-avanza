package jobs;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;

import jobs.qsar.RScriptLauncher;
import jobs.qsar.RScriptLauncherImpl;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import utils.TemplatedConfiguration;
import utils.scripts.ExternalScript;

public class RScriptLauncherTest extends UnitTest{
	
	ExternalScript launcher;
	RScriptLauncher rscriptLauncher;
	
	@Before
	public void setup(){
		launcher = createNiceMock(ExternalScript.class);
		rscriptLauncher = new RScriptLauncherImpl("QSAR");
		rscriptLauncher.setLauncher(launcher);
	}
	
	@Test
	public void launchRscript(){
		rscriptLauncher.addParameters("param1");
		rscriptLauncher.addParameters("param2");
		String command = String.format(TemplatedConfiguration.get("performanceMetrics"), "param1 param2");
		expect(launcher.paralelizeExclusive(/*1, 15360, */"QSAR", command, true)).andReturn("nothing");
		replay(launcher);
		rscriptLauncher.launchRScript(TemplatedConfiguration.get("performanceMetrics"));
		verify(launcher);
	}

	@Test
	public void launchRscriptWithParamatersList(){
		List<String> paramatersList = new ArrayList<String>();
		paramatersList.add("param1");
		paramatersList.add("param2");
		rscriptLauncher.addParameters(paramatersList);
		String command = String.format(TemplatedConfiguration.get("performanceMetrics"), "param1 param2");
		expect(launcher.paralelizeExclusive(/*1, 15360, */"QSAR", command, true)).andReturn("nothing");
		replay(launcher);
		rscriptLauncher.launchRScript(TemplatedConfiguration.get("performanceMetrics"));
		verify(launcher);
	}

}
