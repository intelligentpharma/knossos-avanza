package utils.SBRAEngine;

import java.util.concurrent.ThreadLocalRandom;

import javax.validation.constraints.AssertTrue;

import org.junit.Test;

import play.Logger;
import play.test.UnitTest;

public class SBRACoreGetEstimationTest extends UnitTest{
	private static final int ITERATIONS = 100;
	private static final int TOLERANCE = 100;
	private static final int DUE_DATE_INCREMENT = (SBRAConstants.CLOUD_INSTANCE_BOOT_TIME * 1000) + 1000;
	
	private long computeDueDate(){
		return System.currentTimeMillis() + DUE_DATE_INCREMENT;
	}
	
	//Number of cloud node assignes should not overpass number of jobs when complexities are huge
	@Test
	public void cloudNodesLimited() {
		SBRAEstimate estimate = SBRACore.getEstimation(1000000000, computeDueDate(), 1, 30);
		Logger.info("--- cloudNodesLimited; complexity: %d", estimate.getCloudNodes());
		assertEquals(30, estimate.getCloudNodes());
	}
	
	@Test
	public void noCloudJobsMeansNoCloudNodes() {
		SBRAEstimate estimate = SBRACore.getEstimation(1, computeDueDate(), 1, 0);
		assertEquals(0, estimate.getCloudNodes());
	}
	
	@Test
	public void noLocalJobsMeansNoLocalNodes() {
		SBRAEstimate estimate = SBRACore.getEstimation(1, computeDueDate(), 0, 1);
		assertEquals(0, estimate.getLocalNodes());
	}
	
	@Test
	public void fillACloudNodeShouldNeedJustACloudNode() {		
		SBRAEstimate estimate = SBRACore.getEstimation(6, computeDueDate(), 0, SBRAConstants.CLOUD_JOB_CHUNK);
		assertEquals(36, estimate.getCloudNodes());
	}
		
	@Test
	public void dontAcceptZeroComplexityJobs() {
		try{
			SBRACore.getBalance(0, 0, -1, 0, 0);
			fail( "dontAcceptZeroComplexityJobs Exception not thrown");
		}
		catch(SBRAException ex){
			//Exception thrown, test passed
		}
	}
		
	@Test 
	public void parallelExecution0() {
		int complexity0 = 10;
		int localJobs0 = 0;
		int cloudJobs0 = 200;
		
		int complexity1 = complexity0;
		int localJobs1 = localJobs0 + 1;
		int cloudJobs1 = cloudJobs0;
		
		SBRAEstimate estimate = SBRACore.getEstimation(complexity0, computeDueDate(), localJobs0, cloudJobs0);
		SBRAEstimate estimate2 = SBRACore.getEstimation(complexity1, computeDueDate(), localJobs1, cloudJobs1);
		
		long deliveryDiff = (estimate2.getDelivery().getTime() - estimate.getDelivery().getTime())/TOLERANCE;
		
		assertEquals("The increment of local jobs should fit in parallel execution with the cloud ones", 0, deliveryDiff);
	}
	
	//No more parallel distribution
//	@Test (expected = SBRAException.class)
//	public void parallelExecution1() {
//		int complexity0 = 10;
//		int localJobs0 = 200;
//		int cloudJobs0 = 0;
//		
//		int complexity1 = complexity0;
//		int localJobs1 = localJobs0;
//		int cloudJobs1 = cloudJobs0 + 1;
//		
//		SBRAEstimate estimate = SBRACore.getEstimation(complexity0, computeDueDate(), localJobs0, cloudJobs0);
//		SBRAEstimate estimate2 = SBRACore.getEstimation(complexity1, computeDueDate(), localJobs1, cloudJobs1);
//		
//		long deliveryDiff = (estimate2.getDelivery().getTime() - estimate.getDelivery().getTime())/TOLERANCE;
//		
//		assertEquals("The increment of cloud jobs should fit in parallel execution with the local ones", 0, deliveryDiff);	
//	}
	
	@Test 
	public void randomComplexityIncrement() {
		for(int i = 0; i < ITERATIONS; i++) {
			int complexity = ThreadLocalRandom.current().nextInt(0, 100);
			int localJobs = ThreadLocalRandom.current().nextInt(0, 100);
			int cloudJobs = ThreadLocalRandom.current().nextInt(0, 100);
			int increment = ThreadLocalRandom.current().nextInt(-complexity, 100);
			
			testIncrementComplexity(complexity, localJobs, cloudJobs, increment);
		}
	}

	@Test 
	public void randomLocalJobsIncrement() {		
		for(int i = 0; i < ITERATIONS; i++) {
			int complexity = ThreadLocalRandom.current().nextInt(0, 100);
			int localJobs = ThreadLocalRandom.current().nextInt(0, 100);
			int cloudJobs = ThreadLocalRandom.current().nextInt(0, 100);
			int increment = ThreadLocalRandom.current().nextInt(-localJobs, 100);
			
			testIncrementLocalJobs(complexity, localJobs, cloudJobs, increment);
		}
	}
	
	@Test 
	public void randomCloudJobsIncrement() {
		for(int i = 0; i < ITERATIONS; i++) {
			int complexity = ThreadLocalRandom.current().nextInt(0, 100);
			int localJobs = ThreadLocalRandom.current().nextInt(0, 100);
			int cloudJobs = ThreadLocalRandom.current().nextInt(0, 100);
			int increment = ThreadLocalRandom.current().nextInt(-cloudJobs, 100);
			
			testIncrementCloudJobs(complexity, localJobs, cloudJobs, increment);
		}
	}
	
	private void testIncrementComplexity(int complexity, int localJobs,  int cloudJobs, int increment) {
		Logger.info("--- testIncrementComplexity; complexity: %d, localJobs: %d, cloudJobs: %d, increment: %d", complexity, localJobs, cloudJobs, increment);
		boolean complexityUp = increment > 0;
		
		SBRAEstimate estimate = SBRACore.getEstimation(complexity, computeDueDate(), localJobs, cloudJobs);
		SBRAEstimate estimate2 = SBRACore.getEstimation(complexity+increment, computeDueDate(), localJobs, cloudJobs);
		
		long deliveryDiff = (estimate2.getDelivery().getTime() - estimate.getDelivery().getTime())/TOLERANCE;
		double costDiff = estimate2.getCost() - estimate.getCost();
		int localNodesDiff = estimate2.getLocalNodes() - estimate.getLocalNodes();
		int cloudNodesDiff = estimate2.getCloudNodes() - estimate.getCloudNodes();
		//Logger.info("testIncrementComplexity %s %s %s %s", deliveryDiff, costDiff, localNodesDiff, cloudNodesDiff);
		assertTrue(complexityUp? deliveryDiff >= 0: deliveryDiff <= 0);
		assertTrue(complexityUp? costDiff >= 0: costDiff <= 0);
		assertTrue(complexityUp? localNodesDiff >= 0: localNodesDiff <= 0);
		assertTrue(complexityUp? cloudNodesDiff >= 0: cloudNodesDiff <= 0);
	}
		
	private void testIncrementLocalJobs(int complexity, int localJobs,  int cloudJobs, int increment) {
		Logger.info("--- testIncrementLocalJobs; complexity: %d, localJobs: %d, cloudJobs: %d, increment: %d", complexity, localJobs, cloudJobs, increment);
		boolean localJobsUp = increment > 0;
		
		SBRAEstimate estimate = SBRACore.getEstimation(complexity, computeDueDate(), localJobs, cloudJobs);
		SBRAEstimate estimate2 = SBRACore.getEstimation(complexity, computeDueDate(), localJobs+increment, cloudJobs);
		
		double costDiff = estimate2.getCost() - estimate.getCost();
		int localNodesDiff = estimate2.getLocalNodes() - estimate.getLocalNodes();
		int cloudNodesDiff = estimate2.getCloudNodes() - estimate.getCloudNodes();
		//Logger.info("testIncrementComplexity %s %s %s %s", costDiff, costDiff, localNodesDiff, cloudNodesDiff);
		assertTrue(localJobsUp? costDiff <= 0: costDiff >= 0);			// More local jobs => less cost		
		assertTrue(localJobsUp? localNodesDiff >= 0: localNodesDiff <= 0);
		assertTrue(localJobsUp? cloudNodesDiff <= 0: cloudNodesDiff >= 0);
	}
	
	private void testIncrementCloudJobs(int complexity, int localJobs,  int cloudJobs, int increment) {
		Logger.info("--- testIncrementCloudJobs; complexity: %d, localJobs: %d, cloudJobs: %d, increment: %d", complexity, localJobs, cloudJobs, increment);
		boolean cloudJobsUp = increment > 0;
		
		SBRAEstimate estimate = SBRACore.getEstimation(complexity, computeDueDate(), localJobs, cloudJobs);
		SBRAEstimate estimate2 = SBRACore.getEstimation(complexity, computeDueDate(), localJobs, cloudJobs+ increment);
		
		double costDiff = estimate2.getCost() - estimate.getCost();
		int localNodesDiff = estimate2.getLocalNodes() - estimate.getLocalNodes();
		int cloudNodesDiff = estimate2.getCloudNodes() - estimate.getCloudNodes();
		
		//Logger.info("testIncrementComplexity %s %s %s %s", costDiff, costDiff, localNodesDiff, cloudNodesDiff);
		assertTrue(cloudJobsUp? costDiff >= 0: costDiff <= 0);			// More cloud jobs => more cost		
		assertTrue(cloudJobsUp? localNodesDiff <= 0: localNodesDiff >= 0);
		assertTrue(cloudJobsUp? cloudNodesDiff >= 0: cloudNodesDiff <= 0);
	}

}
