package utils.scripts;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import utils.TemplatedConfiguration;
import utils.scripts.SlurmExternalScript;

public class SlurmExternalScriptTest extends UnitTest {

	SlurmExternalScript launcher;
	
	@Before
	public void setUp() {
		launcher = new SlurmExternalScript();
	}

	@Test
	public void jobNameWithSpacesThrowsException(){
		try{
			launcher.paralelize("with space", "command never to be executed");
			fail("Exception not thrown");
		}catch(Exception e) {
			assertEquals("Job name cannot contain spaces", e.getMessage());
		}
	}
	
	@Test
	public void launchesSomeCommandOnSlurm(){
		try{
			String output = launcher.paralelize("job", "ls -ls");
			assertNotNull(output);
		}catch(RuntimeException e){
			String srunCommand = TemplatedConfiguration.get("srun");
			assertEquals("External Script failed - "+srunCommand+" -D /tmp -n 1 -c 1 -s -J test__job --  ls -ls", e.getMessage());
		}
	}
	
	@Test
	public void launchesSomeCommandOnSlurmWithPartition(){
		try{
			String output = launcher.paralelize("job", "partition", "ls -ls");
			assertNotNull(output);
		}catch(RuntimeException e){
			String srunCommand = TemplatedConfiguration.get("srun");
			assertEquals("External Script failed - "+srunCommand+" -D /tmp -n 1 -c 1 -s -J test__job -p partition --  ls -ls", e.getMessage());
		}
	}
	
	@Test
	public void launchesSomeCommandOnSlurmWithCPUs(){
		try{
			String output = launcher.paralelize(2, "job", "ls -ls");
			assertNotNull(output);
		}catch(RuntimeException e){
			String srunCommand = TemplatedConfiguration.get("srun");
			assertEquals("External Script failed - "+srunCommand+" -D /tmp -n 1 -c 2 -s -J test__job --  ls -ls", e.getMessage());
		}
	}
	
	@Test
	public void launchesSomeCommandOnSlurmWithCPUAndMemory(){
		try{
			String output = launcher.paralelizeExclusive("job", "ls -ls", true);
			assertNotNull(output);
		}catch(RuntimeException e){
			String srunCommand = TemplatedConfiguration.get("srun");
			assertEquals("External Script failed - "+srunCommand+" -D /tmp -n 1 --exclusive -J test__job --  ls -ls", e.getMessage());
		}
	}
	
	@Test
	public void launchesSomeCommandOnSlurmWithTimeout(){
		try{
			String output = launcher.paralelizeWithTimeout("job", "partition", "ls -ls");
			assertNotNull(output);
		}catch(RuntimeException e){
			String srunCommand = TemplatedConfiguration.get("srun");
			assertEquals("External Script failed - "+srunCommand+" -D /tmp -n 1 -c 1 -s -J test__job -p partition --  ls -ls", e.getMessage());
		}
	}
	
	
}
