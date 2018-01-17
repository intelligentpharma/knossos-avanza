package utils.scripts;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import utils.scripts.ExternalScript;
import utils.scripts.ExternalScriptViaCommandLine;

public class ExternalScriptViaCommandLineTest extends UnitTest{

	ExternalScript launcher;
	public ExternalScriptViaCommandLine externalScriptViaCommandLineLauncher;
	
	@Before
	public void setup() {
		launcher = new ExternalScriptViaCommandLine();
		externalScriptViaCommandLineLauncher = new ExternalScriptViaCommandLine();
	}
	
	@Test
	public void launchesScript() throws IOException{
		String result = launcher.launch("ls");
		assertNotNull(result);
	}
	
//	@Test
//	public void launchesScriptThroughArray() throws IOException{
//		String[] command =  {
//	            "/bin/sh",	            
//	            "ls"};   
//		String result = externalScriptViaCommandLineLauncher.launch(command);
//		assertNotNull(result);
//	}
	
	@Test
	public void stringToSecondsConvertsOk(){
		String stringToConvert = "1-02:35:52";
		long seconds = externalScriptViaCommandLineLauncher.stringToSeconds(stringToConvert);
		assertEquals(95752, seconds);
	}
	
	@Test
	public void parallelLaunchingIsTheSameAsRegularLaunching() {
		assertEquals(launcher.launch("ls"), launcher.paralelize("JOB", "ls"));
		assertEquals(launcher.launch("ls"), launcher.paralelize(5, "JOB", "ls"));
	}
	
	@Test
	public void throwsExceptionForFailedCommands(){
		try{
			launcher.launch("mkdir /root/impossible");
			fail("Should throw exception");
		}catch(RuntimeException e){
			//OK
		}
	}

	@Test
	public void launchAndIgnoreErrorsDoesNotThrowException(){
		String output = launcher.launchAndIgnoreErrors("mkdir /root/impossible");
		assertNotNull(output);
	}
	
	@Test
	public void launchAndIgnoreErrorsWithProcessBuilderDoesNotThrowException(){
		String output = launcher.launchAndIgnoreErrorsWithProcessBuilder("mkdir /root/impossible");
		assertNotNull(output);
	}
//TODO: test public void killHangedProcesses(long timeOutInSeconds, String stringToGrep)	
}
