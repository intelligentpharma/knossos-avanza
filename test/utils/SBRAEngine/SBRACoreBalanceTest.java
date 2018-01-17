package utils.SBRAEngine;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.processing.RoundEnvironment;
import javax.persistence.Query;
import javax.validation.constraints.AssertTrue;

import org.easymock.EasyMock;
import org.junit.Test;

import models.MoleculeDatabase;
import models.Deployment;
import play.Logger;
import play.db.jpa.JPABase;
import play.test.UnitTest;

public class SBRACoreBalanceTest extends UnitTest{
	private static final long ONE_HOUR_COMPLEX = 3600000; // time in seconds!

	
	/**
	 * SBRA analyzes an experiment where the number of jobs is less than the number of available local computational nodes. Expects the following results:
	 * SBRA Balancer suggests that all jobs in the experiment are assigned to local nodes: The number of local jobs assigned to the experiment equals the number of jobs in the experiment
	 * SBRA Balancer suggests that no jobs in the experiment are assigned to local nodes
	 * SBRA Estimator predicts that no cloud nodes will be used when running the experiment
	 * 
	 * Formally:
	 * @result SBRABalance.getLocalJobs() == experiment.targetSize * experiment.probeSize
	 * @result SBRABalance.getCloudJobs() == 0
	 * @result SBRAEstimate.getCloudNodes() == 0
	 */
	@Test
	public void lessJobsThanLocalMeansNoCloudNodes() throws SBRAException {
		long targetSize = 1;
		long probeSize  = 9;
		long jobs = targetSize * probeSize;
		long jobcomplexity = ONE_HOUR_COMPLEX; 
		long availableTime = Long.MAX_VALUE;
		long budget = Long.MAX_VALUE; 
		
		SBRABalance balance1 = SBRACore.getBalance(probeSize, targetSize, jobcomplexity, availableTime, budget);		
		SBRAEstimate estimate1 = SBRACore.getEstimation(jobcomplexity, availableTime, balance1.getLocalJobs(), balance1.getCloudJobs());		
		assertEquals(balance1.getLocalJobs(), jobs);
		assertEquals(balance1.getCloudJobs(), 0);
		assertEquals(estimate1.getCloudNodes(), 0);
	}	
	
	/**
	 * SBRA analyzes an experiment where there is no budget available for running cloud experiments. 
	 * In particular the budget is less than the cost of running a cloud instance during a charging chunk (e.g., cost is 1€ per hour and budget is 0.99€). 
	 * Please, note due to provider's charging policy, it is not possible to run cloud instances for less time than a charging chunk (e.g., pay 0.5€ for 30 minutes). Expects the following results:
	 * SBRA Balancer suggests that all jobs in the experiment are assigned to local nodes
	 * SBRA Balancer suggests that no jobs in the experiment are assigned to local nodes
	 * SBRA Estimator predicts that no cloud nodes will be used when running the experiment
	 * 
	 * Formally:
	 * @result SBRABalance.getLocalJobs() == experiment.targetSize * experiment.probeSize
	 * @result SBRABalance.getCloudJobs() == 0
	 * @result SBRAEstimate.getCloudNodes() == 0
	 */
	@Test
	public void noBudgetMeansNoCloudNodes() throws SBRAException {
		long targetSize = 1;
		long probeSize  = 1000;
		long jobs = targetSize * probeSize;
		long jobcomplexity = ONE_HOUR_COMPLEX; 
		long availableTime = Long.MAX_VALUE;
		long budget = (long) Math.floor(SBRAConstants.CLOUD_CHUNK_COST) - 1; // Budget limited to less than 1 cloud job
		
		SBRABalance balance1 = SBRACore.getBalance(probeSize, targetSize, jobcomplexity, availableTime, budget);
		SBRAEstimate estimate1 = SBRACore.getEstimation(jobcomplexity, availableTime, balance1.getLocalJobs(), balance1.getCloudJobs());
		
		assertEquals(balance1.getLocalJobs(), jobs);
		assertEquals(balance1.getCloudJobs(), 0);
		assertEquals(estimate1.getCloudNodes(), 0);
	}
	
	/**
	 * SBRA analyzes an experiment where the cloud chunk can fit an extra job. The idea is checking SBRA fits as many cloud jobs as possible in a single cloud instance.
	 * Please, note due to provider's charging policy, it is not possible to run cloud instances for less time than a charging chunk (e.g., pay 0.5€ for 30 minutes), 
	 * so SBRA is designed to try to fill cloud chunks with experiments, effectively avoiding the creation of extra cloud nodes and therefore minimizing the cost of every job run in the cloud
	 * Expects the following results:
	 * SBRA Balancer suggests that all available local nodes are filled with experiment jobs.
	 * SBRA Balancer suggests that the rest of experiment jobs (the ones that can not be assigned to a local node) are assigned to a cloud nodes.
	 * SBRA Estimator predicts only one cloud node is created, because the chunk still has room for more cloud jobs.
	 * 
	 * Formally:
	 * @result SBRABalance.getLocalJobs() == SBRAConstants.LOCAL_NODES
	 * @result SBRABalance.getCloudJobs() == SBRAConstants.CLOUD_JOB_POWER + 1
	 * @result SBRAEstimate.getCloudNodes() == 1
	 */
	@Test
	public void timeForOneMoreInTheCloudChunk() throws SBRAException {
		long targetSize = 1;
		long probeSize  = (long) (1 + SBRAConstants.LOCAL_NODES + SBRAConstants.CLOUD_JOB_POWER);
		long jobcomplexity = (long) ((ONE_HOUR_COMPLEX  / (SBRAConstants.CLOUD_JOB_POWER + 1)));
		long currentTime = System.currentTimeMillis();
		long availableTime = currentTime + jobcomplexity*1000 + 1000 + (SBRAConstants.CLOUD_INSTANCE_BOOT_TIME * 1000);		
		long budget = Long.MAX_VALUE; 						
		try{	
			SBRABalance balance1 = SBRACore.getBalance(probeSize, targetSize, jobcomplexity, availableTime, budget);													 			
			SBRAEstimate estimate1 = SBRACore.getEstimation(jobcomplexity / 1000, availableTime , balance1.getLocalJobs(), balance1.getCloudJobs());
			assertEquals((int) SBRAConstants.LOCAL_NODES, balance1.getLocalJobs());
			assertEquals(SBRAConstants.CLOUD_JOB_POWER + 1, balance1.getCloudJobs(),0);
			assertEquals(1, estimate1.getCloudNodes());
		}
		catch (SBRAException E){
			Logger.info(E.getMessage());			
			fail();
		}
		
		
	}
	
	/**
	 * SBRA analyzes an experiment where the due date is too close, so there is very few time available for running the experiment. Therefore, SBRA is designed to assign all experiment jobs to cloud nodes
	 * and execute them in as many cloud instances as required.
	 * Expects the following results:
	 * SBRA Balancer suggests that no experiment jobs are assigned to local nodes, allowing them to be completly paralelized in cloud instances.
	 * SBRA Balancer suggests all experiment jobs are assigned to a cloud nodes.
	 * SBRA Estimator predicts that sufficient cloud instances will be created to run the experiment using only one chunk per instance
	 * 
	 * Formally:
	 * @result SBRABalance.getLocalJobs() == 0
	 * @result SBRABalance.getCloudJobs() == experiment.targetSize * experiment.probeSize
	 * @result SBRAEstimate.getCloudNodes() == ((experiment.targetSize * experiment.probeSize) * (jobcomplexity / 1000)) / SBRAConstants.CLOUD_JOB_CHUNK
	 */
	@Test
	public void noTimeMeansAllInParallelCloudNodes() throws SBRAException {
		long targetSize = 1;
		long probeSize  = 1000;
		long jobs = targetSize * probeSize;
		long jobcomplexity = ONE_HOUR_COMPLEX;
		long currentTime = System.currentTimeMillis();
		long availableTime = currentTime + 6000000 + (SBRAConstants.CLOUD_INSTANCE_BOOT_TIME * 1000);
		long budget = Long.MAX_VALUE;
		
		SBRABalance balance1 = SBRACore.getBalance(probeSize, targetSize, jobcomplexity, availableTime , budget);
		SBRAEstimate estimate1 = SBRACore.getEstimation(jobcomplexity / 1000, availableTime , balance1.getLocalJobs(), balance1.getCloudJobs());
		
		assertEquals(0, balance1.getLocalJobs());
		assertEquals(jobs, balance1.getCloudJobs());
		assertEquals(599, estimate1.getCloudNodes());
	}
	

	/**
	 * SBRA analyzes an experiment where the due date is very close, so there is just enough time available for running the experiment in one cloud chunk.
	 *  Therefore, SBRA is designed to assign as many experiment jobs as possible to local nodes and the rest to cloud nodes and execute them in as many cloud instances as required.
	 *  The number of experiment jobs is equal the number of available local nodes plus the number of experiments a cloud chunk can run
	 * Expects the following results:
	 * SBRA Balancer suggests that all local computational nodes are filled with experiment's jobs
	 * SBRA Balancer suggests that the rest of experiment jobs (the ones that can not be assigned to a local node) are assigned to a cloud nodes.
	 * SBRA Estimator predicts that sufficient cloud instances will be created to run the experiment using only one chunk per instance
	 * 
	 * Formally:
	 * @result SBRABalance.getLocalJobs() == SBRAConstants.LOCAL_NODES
	 * @result SBRABalance.getCloudJobs() == SBRAConstants.CLOUD_JOB_POWER == (experiment.targetSize * experiment.probeSize) -  SBRAConstants.LOCAL_NODES
	 * @result SBRAEstimate.getCloudNodes() == 1
	 */
	@Test
	public void fillAllLocalAndOneChunk() throws SBRAException {
		
		long targetSize = 1;
		long probeSize  = (long) (SBRAConstants.LOCAL_NODES + SBRAConstants.CLOUD_JOB_POWER);
		long jobcomplexity = ONE_HOUR_COMPLEX / 1000;
		long currentTime = System.currentTimeMillis();		
		long availableTime = currentTime + (SBRAConstants.CLOUD_INSTANCE_BOOT_TIME * 1000) + (ONE_HOUR_COMPLEX * 1000) ;		
		long budget = Long.MAX_VALUE;  

		SBRABalance balance1 = SBRACore.getBalance(probeSize, targetSize, jobcomplexity * 1000 , availableTime, budget);		
		SBRAEstimate estimate1 = SBRACore.getEstimation(jobcomplexity, availableTime, balance1.getLocalJobs(), balance1.getCloudJobs());
		
		assertEquals((int) SBRAConstants.LOCAL_NODES, balance1.getLocalJobs());
		assertEquals((int) SBRAConstants.CLOUD_JOB_POWER, balance1.getCloudJobs());
		assertEquals(6, estimate1.getCloudNodes());
	}	


	/**
	 * SBRA analyzes an experiment where the due date is very close, so there is just enough time available for running the experiment in one cloud chunk.
	 * Therefore, SBRA is designed to assign as many experiment jobs as possible to local nodes and the rest to cloud nodes and execute them in as many cloud instances as required.
	 * However, there are so many experiments, only one cloud instance can not support them, therefore a second cloud instance is created in parallel. 
	 * The number of experiment jobs is equal the number of available local nodes plus the number of experiments a cloud chunk can run plus one (so the first cloud chunk overflows)
	 * Expects the following results:
	 * SBRA Balancer suggests that all local computational nodes are filled with experiment's jobs
	 * SBRA Balancer suggests that the rest of experiment jobs (the ones that can not be assigned to a local node) are assigned to a cloud nodes.
	 * SBRA Estimator predicts that sufficient cloud instances will be created to run the experiment using only one chunk per instance
	 * 
	 * Formally:
	 * @result SBRABalance.getLocalJobs() == SBRAConstants.LOCAL_NODES
	 * @result SBRABalance.getCloudJobs() == SBRAConstants.CLOUD_JOB_POWER + 1 == (experiment.targetSize * experiment.probeSize) -  SBRAConstants.LOCAL_NODES + 1
	 * @result SBRAEstimate.getCloudNodes() == 2
	 */
	@Test
	public void fillAllLocalOneChunkAndOneMoreCloudJob() throws SBRAException {
		
		long targetSize = 1;
		long probeSize  = (long) (SBRAConstants.LOCAL_NODES + SBRAConstants.CLOUD_JOB_POWER) + 1;
		long jobcomplexity = ONE_HOUR_COMPLEX / 1000;
		long currentTime = System.currentTimeMillis();				
		long availableTime = currentTime + 6000000 + (SBRAConstants.CLOUD_INSTANCE_BOOT_TIME * 1000) + (jobcomplexity * 1000000);
		long budget = Long.MAX_VALUE;  
		
		Logger.info("getBalance %s %s %s %s %s", probeSize, targetSize, jobcomplexity, availableTime , budget);
		SBRABalance balance1 = SBRACore.getBalance(probeSize, targetSize, jobcomplexity * 1000, availableTime , budget);
		SBRAEstimate estimate1 = SBRACore.getEstimation(jobcomplexity, availableTime, balance1.getLocalJobs(), balance1.getCloudJobs());
		
		assertEquals((int) SBRAConstants.LOCAL_NODES, balance1.getLocalJobs());
		assertEquals((int) SBRAConstants.CLOUD_JOB_POWER + 1, balance1.getCloudJobs());
		assertEquals(7, estimate1.getCloudNodes());
	}
	
	/**
	 * SBRA analyzes an experiment where the due date is very far away, so even though there is enough budget to run cloud instances, all jobs are assigned to local nodes, as there is enough
	 * time available for running the experiment. It means SBRA is dessigned to prioritize using time over budget.
	 * Expects the following results:
	 * SBRA Balancer suggests that experiment jobs are assigned to local nodes.
	 * SBRA Balancer suggests that no experiment jobs are assigned to cloud nodes.
	 * SBRA Estimator predicts that no cloud instances will be used
	 * 
	 * Formally:
	 * @result SBRABalance.getLocalJobs() == experiment.targetSize * experiment.probeSize  == 1 * experiment.probeSize == experiment.probeSize
	 * @result SBRABalance.getCloudJobs() == 0
	 * @result SBRAEstimate.getCloudNodes() == 0
	 */
	@Test
	public void sequencialBalanceControlledBudget() throws SBRAException {
		
		long targetSize = 1;
		long probeSize  = SBRAConstants.LOCAL_NODES + 50;
		long jobcomplexity = (ONE_HOUR_COMPLEX - (SBRAConstants.CLOUD_INSTANCE_BOOT_TIME / SBRAConstants.CLOUD_JOB_CHUNK)) / 1000;
		long currentTime = System.currentTimeMillis();		
		long availableTime = currentTime + 6000000 + (SBRAConstants.CLOUD_INSTANCE_BOOT_TIME * 1000) + (jobcomplexity * 1000000);	
		long budget = (long)(SBRAConstants.CLOUD_CHUNK_COST * 50);  

		SBRABalance balance1 = SBRACore.getBalance(probeSize, targetSize, jobcomplexity * 1000, availableTime , budget);
		SBRAEstimate estimate1 = SBRACore.getEstimation(jobcomplexity, availableTime , balance1.getLocalJobs(), balance1.getCloudJobs());
		
		assertEquals((int) SBRAConstants.LOCAL_NODES, balance1.getLocalJobs());
		assertEquals(50, balance1.getCloudJobs());
		assertEquals(30, estimate1.getCloudNodes());
		
		// And all in local if we have infinite time
		availableTime = Long.MAX_VALUE; 
		
		balance1 = SBRACore.getBalance(probeSize, targetSize, jobcomplexity, availableTime, budget);
		estimate1 = SBRACore.getEstimation(jobcomplexity, availableTime, balance1.getLocalJobs(), balance1.getCloudJobs());
		
		assertEquals(probeSize, balance1.getLocalJobs());
		assertEquals(0, balance1.getCloudJobs());
		assertEquals(0, estimate1.getCloudNodes());
	}
	
	/**
	 * SBRA analyzes an experiment where the due date is too close, so there is very few time available for running the experiment.
	 * However, the budget for running the experiment is tightly limited as well, there is only budget for running one cloud instance during a cloud chunk
	 * Therefore, even though the due date is not met, all jobs 
	 * Expects the following results:
	 * SBRA Balancer suggests that all local nodes are filled with experiment jobs.
	 * SBRA Balancer suggests that the rest of experiment jobs are assigned to cloud instances, filling exactly one cloud instance during one chunk.
	 * SBRA Estimator predicts that only once cloud instance is created.
	 * 
	 * Formally:
	 * @result SBRABalance.getLocalJobs() == SBRAConstants.LOCAL_NODES
	 * @result SBRABalance.getCloudJobs() == SBRAConstants.CLOUD_JOB_POWER
	 * @result SBRAEstimate.getCloudNodes() == 1
	 * 
	 * The test goes ahead by simulating a new experiment with one more job w.r.t. the previous test. SBRA analyzes the experiment and realizes no more jobs can be assigned to cloud instances
	 * because they don't fit in the original chunk and there is no budget for another instance. Therefore, the number of local jobs increases by one, overpassing the due date.
	 * Expects the following results:
	 * SBRA Balancer suggests that all local nodes plus one are filled with experiment jobs, so the due date is overpassed.
	 * SBRA Balancer suggests that the rest of experiment jobs are assigned to cloud instances, filling exactly one cloud instance during one chunk.
	 * SBRA Estimator predicts that only once cloud instance is created.
	 * 
	 * * Formally:
	 * @result SBRABalance.getLocalJobs() == SBRAConstants.LOCAL_NODES + 1
	 * @result SBRABalance.getCloudJobs() == SBRAConstants.CLOUD_JOB_POWER
	 * @result SBRAEstimate.getCloudNodes() == 1
	 * 
	 * The test proposes another situation by replicating the previous one with infinite budget. The extra job that was assigned to local nodes is assigned now to cloud nodes, and as it does not fit
	 * in a single chunk a new cloud instance is created, effectively meeting the due date.
	 * Expects the following results:
	 * SBRA Balancer suggests that all local nodes are filled with experiment jobs, so the due date is met.
	 * SBRA Balancer suggests that the rest of experiment jobs are assigned to cloud instances, filling two cloud instances during one chunk each.
	 * SBRA Estimator predicts that only two cloud instances are created.
	 * 
	 * * Formally:
	 * @result SBRABalance.getLocalJobs() == SBRAConstants.LOCAL_NODES 
	 * @result SBRABalance.getCloudJobs() == SBRAConstants.CLOUD_JOB_POWER + 1
	 * @result SBRAEstimate.getCloudNodes() == 2
	 */
	@Test
	public void sequencialBalanceWithBudgetLimitedToMaxOneCloudChunk() throws SBRAException {
		
		long targetSize = 1;
		long probeSize  = (long) (SBRAConstants.LOCAL_NODES + SBRAConstants.CLOUD_JOB_POWER);
		long jobcomplexity = ONE_HOUR_COMPLEX;
		long currentTime = System.currentTimeMillis();		
		long availableTime = currentTime + 6000000 + (SBRAConstants.CLOUD_INSTANCE_BOOT_TIME * 1000) + (jobcomplexity * 1000000);
		long budget = (long) SBRAConstants.CLOUD_CHUNK_COST;  

		SBRABalance balance1 = SBRACore.getBalance(probeSize, targetSize, jobcomplexity * 1000, availableTime , budget*5);
		SBRAEstimate estimate1 = SBRACore.getEstimation(jobcomplexity , availableTime , balance1.getLocalJobs(), balance1.getCloudJobs());
		
		assertEquals(10, balance1.getLocalJobs());
		assertEquals(10, balance1.getCloudJobs());
		assertEquals(10, estimate1.getCloudNodes());
		
		// Now, adding one job, local jobs should increase by 1, because we do not have more budget.
		long probeSizePlus1  = probeSize + 1;  
		SBRABalance balance2 = SBRACore.getBalance(probeSizePlus1, targetSize, jobcomplexity * 1000, availableTime , budget*5);
		SBRAEstimate estimate2 = SBRACore.getEstimation(jobcomplexity , availableTime , balance2.getLocalJobs(), balance2.getCloudJobs());
		
		assertEquals(10, balance2.getLocalJobs());
		assertEquals(11, balance2.getCloudJobs());
		assertEquals(11, estimate2.getCloudNodes());
		
		// Finally, having infinite budget, balancer should add the job to the cloud and create a new node
		budget = Long.MAX_VALUE;
		SBRABalance balance3 = SBRACore.getBalance(probeSizePlus1, targetSize, jobcomplexity*1000, availableTime , budget*5);
		SBRAEstimate estimate3 = SBRACore.getEstimation(jobcomplexity , availableTime , balance3.getLocalJobs(), balance3.getCloudJobs());
		
		assertEquals((int) SBRAConstants.LOCAL_NODES, balance3.getLocalJobs());
		assertEquals(1 + (int) SBRAConstants.CLOUD_JOB_POWER, balance3.getCloudJobs());
		assertEquals(11, estimate3.getCloudNodes());
	}
		
}
