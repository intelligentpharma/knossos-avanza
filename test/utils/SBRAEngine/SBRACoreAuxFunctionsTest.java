package utils.SBRAEngine;

import java.util.Vector;

import org.junit.Test;

import play.Logger;
import play.test.UnitTest;
import utils.SBRAEngine.SBRACore.ExperimentTime;
import utils.SBRAEngine.SBRACore.SequentialInstanceSet;

public class SBRACoreAuxFunctionsTest extends UnitTest{
	
	@Test
	//Tests Distribution of jobs in parallel
	public void testParallelDistribution() {
		long cloudJobs;
		Integer chunk;				
		
		
		cloudJobs = 0;
		chunk = 5;
		Logger.info("Testing Parallel distribution with jobs %s chunks %s", cloudJobs, chunk);
		assertEquals(0, SBRACore.distributeJobsInParalell(cloudJobs,chunk));
				
		cloudJobs = 2;
		chunk = 5;
		Logger.info("Testing Parallel distribution with jobs %s chunks %s", cloudJobs, chunk);
		assertEquals(1, SBRACore.distributeJobsInParalell(cloudJobs,chunk));
		
		cloudJobs = 5;
		chunk = 5;
		Logger.info("Testing Parallel distribution with jobs %s chunks %s", cloudJobs, chunk);
		assertEquals(1, SBRACore.distributeJobsInParalell(cloudJobs,chunk));
		
		cloudJobs = 7;
		chunk = 5;
		Logger.info("Testing Parallel distribution with jobs %s chunks %s", cloudJobs, chunk);
		assertEquals(2, SBRACore.distributeJobsInParalell(cloudJobs,chunk));
	}
	
	@Test
	//Tests Distribution of jobs in sequential
	public void testSequentialDistribution() {
		int cloudJobs;
		Integer chunk;		
		long availableTime;
		Vector<SequentialInstanceSet> sequentialInstaces;
		
		
		cloudJobs = 0;
		chunk = 5;
		availableTime = 0;
		Logger.info("Testing Sequential distribution with jobs %s chunks %s availableTime %s", cloudJobs, chunk, availableTime);
		sequentialInstaces = SBRACore.distributeJobsInSequential(cloudJobs,chunk,availableTime);
		assertEquals(0, sequentialInstaces.size());
		
		cloudJobs = 5;
		chunk = 5;
		availableTime = SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION;
		Logger.info("Testing Sequential distribution with jobs %s chunks %s availableTime %s", cloudJobs, chunk, availableTime);
		sequentialInstaces = SBRACore.distributeJobsInSequential(cloudJobs,chunk,availableTime);
		assertEquals(1, sequentialInstaces.size());
		assertEquals(5, sequentialInstaces.get(0).jobs);
		assertEquals(1, sequentialInstaces.get(0).hours);
		
		cloudJobs = 5;
		chunk = 5;
		availableTime = SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION + 10;
		Logger.info("Testing Sequential distribution with jobs %s chunks %s availableTime %s", cloudJobs, chunk, availableTime);
		sequentialInstaces = SBRACore.distributeJobsInSequential(cloudJobs,chunk,availableTime);
		assertEquals(1, sequentialInstaces.size());
		assertEquals(5, sequentialInstaces.get(0).jobs);
		assertEquals(1, sequentialInstaces.get(0).hours);
		
		cloudJobs = 10;
		chunk = 5;
		availableTime = SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION;
		Logger.info("Testing Sequential distribution with jobs %s chunks %s availableTime %s", cloudJobs, chunk, availableTime);
		sequentialInstaces = SBRACore.distributeJobsInSequential(cloudJobs,chunk,availableTime);
		assertEquals(2, sequentialInstaces.size());
		assertEquals(5, sequentialInstaces.get(0).jobs);
		assertEquals(1, sequentialInstaces.get(0).hours);
		assertEquals(5, sequentialInstaces.get(1).jobs);
		assertEquals(1, sequentialInstaces.get(1).hours);
		
		cloudJobs = 8;
		chunk = 5;
		availableTime = SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION + 10;
		Logger.info("Testing Sequential distribution with jobs %s chunks %s availableTime %s", cloudJobs, chunk, availableTime);
		sequentialInstaces = SBRACore.distributeJobsInSequential(cloudJobs,chunk,availableTime);
		assertEquals(2, sequentialInstaces.size());
		assertEquals(5, sequentialInstaces.get(0).jobs);
		assertEquals(1, sequentialInstaces.get(0).hours);
		assertEquals(3, sequentialInstaces.get(1).jobs);
		assertEquals(1, sequentialInstaces.get(1).hours);
		
		cloudJobs = 8;
		chunk = 5;
		availableTime = (SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION * 2) + 1;
		Logger.info("Testing Sequential distribution with jobs %s chunks %s availableTime %s", cloudJobs, chunk, availableTime);
		sequentialInstaces = SBRACore.distributeJobsInSequential(cloudJobs,chunk,availableTime);
		assertEquals(1, sequentialInstaces.size());
		assertEquals(8, sequentialInstaces.get(0).jobs);
		assertEquals(2, sequentialInstaces.get(0).hours);
		
		cloudJobs = 11;
		chunk = 1;
		availableTime = SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION * 4;
		Logger.info("Testing Sequential distribution with jobs %s chunks %s availableTime %s", cloudJobs, chunk, availableTime);
		sequentialInstaces = SBRACore.distributeJobsInSequential(cloudJobs,chunk,availableTime);
		assertEquals(3, sequentialInstaces.size());
		assertEquals(4, sequentialInstaces.get(0).jobs);
		assertEquals(4, sequentialInstaces.get(0).hours);
		assertEquals(4, sequentialInstaces.get(1).jobs);
		assertEquals(4, sequentialInstaces.get(1).hours);
		assertEquals(3, sequentialInstaces.get(2).jobs);
		assertEquals(3, sequentialInstaces.get(2).hours);
		
		cloudJobs = 55;
		chunk = 5;
		availableTime = SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION * 4;
		Logger.info("Testing Sequential distribution with jobs %s chunks %s availableTime %s", cloudJobs, chunk, availableTime);
		sequentialInstaces = SBRACore.distributeJobsInSequential(cloudJobs,chunk,availableTime);
		assertEquals(3, sequentialInstaces.size());
		assertEquals(20, sequentialInstaces.get(0).jobs);
		assertEquals(4, sequentialInstaces.get(0).hours);
		assertEquals(20, sequentialInstaces.get(1).jobs);
		assertEquals(4, sequentialInstaces.get(1).hours);
		assertEquals(15, sequentialInstaces.get(2).jobs);
		assertEquals(3, sequentialInstaces.get(2).hours);
		
		cloudJobs = 41;
		chunk = 5;
		availableTime = SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION * 4;
		Logger.info("Testing Sequential distribution with jobs %s chunks %s availableTime %s", cloudJobs, chunk, availableTime);
		sequentialInstaces = SBRACore.distributeJobsInSequential(cloudJobs,chunk,availableTime);
		assertEquals(3, sequentialInstaces.size());
		assertEquals(15, sequentialInstaces.get(0).jobs);
		assertEquals(3, sequentialInstaces.get(0).hours);
		assertEquals(15, sequentialInstaces.get(1).jobs);
		assertEquals(3, sequentialInstaces.get(1).hours);
		assertEquals(11, sequentialInstaces.get(2).jobs);
		assertEquals(3, sequentialInstaces.get(2).hours);
		
		
	}
	
	@Test
	//Tests Distribution of jobs in sequential with overhead
	public void testSequentialDistributionOverhead() {
	
		int cloudJobs;
		Integer chunk;		
		long availableTime;
		Vector<SequentialInstanceSet> sequentialInstaces;
		
		cloudJobs = 0;
		chunk = 5;
		availableTime = 0;
		Logger.info("Testing Sequential distribution with jobs %s chunks %s availableTime %s", cloudJobs, chunk, availableTime);
		sequentialInstaces = SBRACore.distributeJobsInSequential(cloudJobs,chunk,availableTime);
		assertEquals(0, sequentialInstaces.size());
		
		cloudJobs = 10;
		chunk = 5;
		availableTime = SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION * 2;
		Logger.info("Testing Sequential distribution with jobs %s chunks %s availableTime %s", cloudJobs, chunk, availableTime);
		sequentialInstaces = SBRACore.distributeJobsInSequentialWithOverhead(cloudJobs,chunk,availableTime);
		assertEquals(2, sequentialInstaces.size());
		assertEquals(5, sequentialInstaces.get(0).jobs);
		assertEquals(1, sequentialInstaces.get(0).hours);
		assertEquals(5, sequentialInstaces.get(1).jobs);
		assertEquals(1, sequentialInstaces.get(1).hours);
		
		cloudJobs = 10;
		chunk = 5;
		availableTime = (SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION * 2) + SBRAConstants.CLOUD_INSTANCE_BOOT_TIME + 10;
		Logger.info("Testing Sequential distribution with jobs %s chunks %s availableTime %s", cloudJobs, chunk, availableTime);
		sequentialInstaces = SBRACore.distributeJobsInSequentialWithOverhead(cloudJobs,chunk,availableTime);
		assertEquals(1, sequentialInstaces.size());
		assertEquals(10, sequentialInstaces.get(0).jobs);
		assertEquals(2, sequentialInstaces.get(0).hours);		
		
		cloudJobs = 10;
		chunk = 5;
		availableTime = (SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION * 2) + SBRAConstants.CLOUD_INSTANCE_BOOT_TIME;
		Logger.info("Testing Sequential distribution with jobs %s chunks %s availableTime %s", cloudJobs, chunk, availableTime);
		sequentialInstaces = SBRACore.distributeJobsInSequentialWithOverhead(cloudJobs,chunk,availableTime);
		assertEquals(1, sequentialInstaces.size());
		assertEquals(10, sequentialInstaces.get(0).jobs);
		assertEquals(2, sequentialInstaces.get(0).hours);	
		
		cloudJobs = 9;
		chunk = 5;
		availableTime = (SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION * 2);
		Logger.info("Testing Sequential distribution with jobs %s chunks %s availableTime %s", cloudJobs, chunk, availableTime);
		sequentialInstaces = SBRACore.distributeJobsInSequentialWithOverhead(cloudJobs,chunk,availableTime);
		assertEquals(2, sequentialInstaces.size());
		assertEquals(5, sequentialInstaces.get(0).jobs);
		assertEquals(1, sequentialInstaces.get(0).hours);
		assertEquals(4, sequentialInstaces.get(1).jobs);
		assertEquals(1, sequentialInstaces.get(1).hours);
		
	}	
	
	@Test
	//Tests Computing experiment time
	public void testExperimentTime() {
		
		int cloudJobs;
		int localJobs;
		long jobcomplexity;
		long availableTime =0;
		SBRAConstants.Distribution distributionMethod;
		ExperimentTime result;
				
		try{
			//Test for normal computation parallel
			distributionMethod = SBRAConstants.Distribution.PARALLEL;
			cloudJobs = 500;
			localJobs = 500;
			jobcomplexity = 100;		
			result = SBRACore.computeExperimentTime(localJobs, cloudJobs, jobcomplexity, availableTime, distributionMethod);		
			fail();
		}
		catch(Exception e){
			e.printStackTrace();			
		}				
		
		try{
			//Test for sequential, extensive sequential tests are in the previous function
			distributionMethod = SBRAConstants.Distribution.SEQUENTIAL;
			cloudJobs = 9;
			localJobs = 500;
			jobcomplexity = 1;		
			availableTime = SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION * 2;
			result = SBRACore.computeExperimentTime(localJobs, cloudJobs, jobcomplexity, availableTime, distributionMethod);
			assertEquals(50000, result.localTime,0);
			//One charge fraction
			assertEquals(SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION, result.cloudTime,0);
		}
		catch(Exception e){
			e.printStackTrace();
			fail();
		}
		
		try{
			//Test for sequential, extensive sequential tests are in the previous function
			distributionMethod = SBRAConstants.Distribution.SEQUENTIAL;
			cloudJobs = (int) (SBRAConstants.CLOUD_JOB_CHUNK + (0.8 * SBRAConstants.CLOUD_JOB_CHUNK));
			localJobs = 500;
			//Casting to long in case charge fraction is double
			jobcomplexity = (long) (SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION / SBRAConstants.CLOUD_JOB_POWER) ;		
			//Two charge fractions plus a little bit of time for testing precision
			availableTime = (SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION * 2) + SBRAConstants.CLOUD_INSTANCE_BOOT_TIME;
			result = SBRACore.computeExperimentTime(localJobs, cloudJobs, jobcomplexity, availableTime, distributionMethod);
			assertEquals(50000 * jobcomplexity, result.localTime,0);
			//Two charge fraction
			assertEquals(SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION * 2, result.cloudTime,0);
		}
		catch(Exception e){
			e.printStackTrace();
			fail();
		}
		
		try{
			//Test for sequential, extensive sequential tests are in the previous function
			distributionMethod = SBRAConstants.Distribution.SEQUENTIAL;
			cloudJobs = (int) ((SBRAConstants.CLOUD_JOB_CHUNK * 2) + (0.8 * SBRAConstants.CLOUD_JOB_CHUNK));
			localJobs = 500;
			jobcomplexity = (long) (SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION / SBRAConstants.CLOUD_JOB_POWER) ;		
			//Two charge fractions plus a little bit of time for testing precision
			availableTime = (SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION * 2) + 1;
			result = SBRACore.computeExperimentTime(localJobs, cloudJobs, jobcomplexity, availableTime, distributionMethod);
			assertEquals(50000 * jobcomplexity, result.localTime,0);
			//Test for sequential, extensive sequential tests are in the previous function
			assertEquals(SBRAConstants.CLOUD_INSTANCE_CHARGE_FRACTION, result.cloudTime,0);
		}
		catch(Exception e){
			e.printStackTrace();
			fail();
		}

		
		
	}
	
	@Test
	//Tests Computing experiment time
	public void testTimeException() {		
		try{
			long targetSize = 1;
			long probeSize  = 1000;			
			long jobcomplexity = 3600;
			long currentTime = System.currentTimeMillis();
			long availableTime = currentTime + SBRAConstants.CLOUD_INSTANCE_BOOT_TIME - 1;
			long budget = Long.MAX_VALUE;
			
			SBRABalance balance1 = SBRACore.getBalance(probeSize, targetSize, jobcomplexity, availableTime, budget);
			fail();
		}
		catch(SBRAException e){
			e.printStackTrace();			
		}	
	}
	
	@Test
	//Tests Computing experiment complexity
	public void testComplexityException() {		
		try{
			long targetSize = 1;
			long probeSize  = 1000;			
			long jobcomplexity = 3600;
			long currentTime = System.currentTimeMillis();
			long availableTime = currentTime + SBRAConstants.CLOUD_INSTANCE_BOOT_TIME + 10000;
			long budget = Long.MAX_VALUE;
			
			SBRABalance balance1 = SBRACore.getBalance(probeSize, targetSize, jobcomplexity, availableTime, budget);
			fail();
		}
		catch(SBRAException e){
			e.printStackTrace();			
		}	
	}
		
}
