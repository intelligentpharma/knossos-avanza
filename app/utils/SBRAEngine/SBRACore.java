


package utils.SBRAEngine;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import models.MoleculeDatabase;
import org.apache.log4j.Logger;
import utils.SBRAEngine.SBRAConstants.Distribution;

public class SBRACore {
	public static final Logger logger = Logger.getLogger(SBRACore.class);
	
	protected static class ExperimentTime {			
	public static final Logger logger = Logger.getLogger(ExperimentTime.class);
		public double localTime;
		public double cloudTime;
		
		public ExperimentTime(double localTime, double cloudTime){
			this.localTime = localTime;
			this.cloudTime = cloudTime;
		}
	}	
		
	public static class SequentialInstanceSet{
	public static final Logger logger = Logger.getLogger(SequentialInstanceSet.class);
		public int jobs;
		public int hours;
		
		public SequentialInstanceSet(int jobs, int hours){
			this.jobs = jobs;
			this.hours = hours;
		}
		
	}
	
	static double getCloudJobChunk(String system){
		if (system.equalsIgnoreCase("Amazon")){
			return (double)SBRAConstants.AMAZON_CLOUD_JOB_CHUNK;
		}
		else if (system.equalsIgnoreCase("Google")){
			return (double)SBRAConstants.GOOGLE_CLOUD_JOB_CHUNK;
		}
		else {
			return (double)SBRAConstants.AZURE_CLOUD_JOB_CHUNK;
		}
	}
	
	static int getBootTime(String system){
		if (system.equalsIgnoreCase("Amazon")){
			return SBRAConstants.AMAZON_INSTANCE_BOOT_TIME;
		}
		else if (system.equalsIgnoreCase("Google")){
			return SBRAConstants.GOOGLE_INSTANCE_BOOT_TIME;
		}
		else {
			return SBRAConstants.AZURE_INSTANCE_BOOT_TIME;
		}
	}
	
	static int getChargeFraction(String system){
		if (system.equalsIgnoreCase("Amazon")){
			return SBRAConstants.AMAZON_INSTANCE_CHARGE_FRACTION;
		}
		else if (system.equalsIgnoreCase("Google")){
			return SBRAConstants.GOOGLE_INSTANCE_CHARGE_FRACTION;
		}
		else {
			return SBRAConstants.AZURE_INSTANCE_CHARGE_FRACTION;
		}		
	}
	
	static double getCost(String system){
		if (system.equalsIgnoreCase("Amazon")){
			return SBRAConstants.AMAZON_CHUNK_COST;
		}
		else if (system.equalsIgnoreCase("Google")){
			return SBRAConstants.GOOGLE_CHUNK_COST;
		}
		else {
			return SBRAConstants.AZURE_CHUNK_COST;
		}						
	}
	
	public static Vector<SequentialInstanceSet> sequentialInstaces;
	
	public static Vector<SequentialInstanceSet> distributeJobsInSequentialWithOverhead(int cloudJobs, int chunk, long availableTime){
		return distributeJobsInSequentialWithOverhead(cloudJobs, chunk, availableTime, "Amazon");
	}
	
	//Distribute cloud jobs in nodes in sequential priority accounting for overhead
	public static Vector<SequentialInstanceSet> distributeJobsInSequentialWithOverhead(int cloudJobs, int chunk, long availableTime, String system){
		Long adjustedAvailableTime =  availableTime - getBootTime(system);
		return distributeJobsInSequential(cloudJobs, chunk, adjustedAvailableTime, system);
	}
	
	public static Vector<SequentialInstanceSet> distributeJobsInSequential(int cloudJobs, int chunk, long availableTime) {
		return distributeJobsInSequential(cloudJobs, chunk, availableTime,"Amazon");
	}
	
	//Distribute cloud jobs in nodes in sequential priority.
	public static Vector<SequentialInstanceSet> distributeJobsInSequential(int cloudJobs, int chunk, long availableTime, String system) {
		
		Vector<SequentialInstanceSet> sequentialInstaces = new Vector<SequentialInstanceSet>();
		
		if (cloudJobs == 0) {
			return sequentialInstaces;
		}
		
		//Number of instances required to run the jobs
		Integer instances = distributeJobsInParalell(cloudJobs, chunk);	
		
		//Time required to run all instances in seq mode one set
		Integer time = instances * getChargeFraction(system);
		Integer instaceSetSize = 1;
		
		//logger.debug(String.format("distributeJobsInSequential time %s availableTime %s instaceSetSize %s steps %s", time,availableTime,instaceSetSize, instances));
		while (time > availableTime) {				
			instaceSetSize++;				
			instances = distributeJobsInParalell((int)Math.ceil(new Float(cloudJobs)/instaceSetSize), chunk);
			time = instances * getChargeFraction(system);
			//logger.debug(String.format("distributeJobsInSequential time %s availableTime %s instaceSetSize %s steps %s", time,availableTime,instaceSetSize, instances));
		}
		
		//Distribute jobs among sequential instances
		int assignedJobs = 0;
		for (int i = 0; i < instaceSetSize; i++) {
			int addJobs = instances * chunk;
			assignedJobs = assignedJobs + addJobs;
			
			if (assignedJobs > cloudJobs) {
				addJobs = (int) (addJobs - (assignedJobs - cloudJobs));
				assignedJobs = cloudJobs;
				
			}
			
			SequentialInstanceSet set = new SequentialInstanceSet(addJobs, distributeJobsInParalell(addJobs, chunk));
			sequentialInstaces.add(set);
		}
		
		int remainingJobs =  cloudJobs - assignedJobs;			
		//Correct jobs assigned to instances if total is not met
		while (remainingJobs > 0) {
			for (SequentialInstanceSet s: sequentialInstaces) {
				if (remainingJobs <= 0) {
					break;
				}
				
				int removeJobs = (chunk > remainingJobs)? remainingJobs: chunk;
				s.jobs = s.jobs + chunk;
				s.hours = s.hours + 1;		
				remainingJobs = remainingJobs - removeJobs;
			}				
		}
		
		int i = 0;
		for(SequentialInstanceSet s: sequentialInstaces) {
			//logger.debug(String.format("distributeJobsInSequential result %s jobs %s steps %s", i, s.jobs, s.hours));
			i++;			
		}	
		
		return sequentialInstaces;
	}
	
	//Distribute cloud jobs in nodes in parallel priority.
	protected static int distributeJobsInParalell(long cloudJobs, int chunk) {		
		//logger.debug(String.format("distributeJobsInParalell %s %s %s ", cloudJobs, chunk, (int) Math.ceil((float) cloudJobs / chunk)));
		return (int) Math.ceil((float) cloudJobs / chunk);
	}
	
	public static SBRABalance getBalance(long probeSize, long targetSize, long jobcomplexity, long duedate, float budget) throws SBRAException {
		return getBalance(probeSize, targetSize, jobcomplexity, duedate, budget, "Amazon");
	}
	
	public static SBRABalance getBalance(long probeSize, long targetSize, long jobcomplexity, long duedate, float budget, String system) throws SBRAException {
		SBRABalance result;
		
		//Compute max number of time the experiment can take
		long currentMillis = System.currentTimeMillis();
		long availableTime = (duedate - (currentMillis + (getBootTime(system) * 1000)));		
		//logger.debug("currentMillis (ms) " + currentMillis + " duedate (ms) " + duedate + " availableTime (s) " + availableTime);			
		
		//If due date is in the past throw exception
		if(availableTime < 0) {
			throw new SBRAException("Your due date is too short availableTime '" + availableTime + "' "
															 + "duedate '" + duedate + "' "
															 + "currentMillis '" + currentMillis + "' "
															 + "CLOUD_INSTANCE_BOOT_TIME '" + getBootTime(system) + "' ");
		}

		if(jobcomplexity < 1) {			
			throw new SBRAException("Your complexity is incorrect, minimum value is 1. jobcomplexity '" + jobcomplexity);
		}
		
		//Compute Number of jobs based on number of deployments and complexity
		int jobs = (int) (probeSize * targetSize);
									
		//Due date priorization asumes budget is unlimited
		if(SBRAConstants.PRIORIZATION_METHOD == SBRAConstants.Priorization.DATE) {
			result =  balance(jobs, availableTime, jobcomplexity, Float.MAX_VALUE, system);
		} else { //Cost priorization is default
			result =  balance(jobs, availableTime, jobcomplexity, budget, system);
		}				
		return result;
	}		
	
	public static SBRAEstimate getEstimation(long jobcomplexity, long duedate, int localbalance, int cloudbalance) {	
		return getEstimation(jobcomplexity, duedate, localbalance, cloudbalance,"Amazon");
	}

	public static SBRAEstimate getEstimation(long jobcomplexity, long duedate, int localbalance, int cloudbalance, String system) {	
		int cloudNodes = 0;
		int localNodes = 0;				
		//Compute max number of time the experiment can take
		long currentMillis = System.currentTimeMillis();
		long availableTime = (duedate - currentMillis);		
		//logger.info("Pelusso getEstimation: " + duedate);
		//Take into account cloud time chunks		
		cloudNodes = distributeJobsInParalell(cloudbalance * jobcomplexity, (int)getCloudJobChunk(system));
		//Take into account local time chunks
		localNodes = distributeJobsInParalell(localbalance * jobcomplexity, SBRAConstants.LOCAL_JOB_CHUNK);		
		
		//Estimate cost and computing time		
		ExperimentTime expTime = computeExperimentTimeSequential(localbalance, cloudbalance, jobcomplexity, availableTime);	
		//logger.info("expTime.localTime " + expTime.localTime + "expTime.cloudTime " + expTime.cloudTime);
		Double time = Math.max(expTime.localTime, expTime.cloudTime);	
		double cost = (localbalance * SBRAConstants.LOCAL_JOB_COST) + ( cloudNodes * getCost(system));	
		if (jobcomplexity > getCloudJobChunk(system)){
			cost = (localbalance * SBRAConstants.LOCAL_JOB_COST) + ( cloudbalance * getCost(system));	
		}	
		cloudNodes = Math.min(cloudNodes, cloudbalance);
		//Logging for debug
		//logger.debug("getEstimation ' localbalance : '" + localbalance + "' cloudbalance : '" + cloudbalance + "' time : '" + time + "'" + "cost : '" + cost + "'" + "'" + "cloudNodes : '" + cloudNodes + "'" + "'" + "localNodes : '" + localNodes + "'" + "jobcomplexity : '" + jobcomplexity + "'" + "SBRAConstants.CLOUD_JOB_CHUNK : '" + getCloudJobChunk(system) + "'" + "|" + availableTime + " system : '" + system + " getCost(system) : '" + getCost(system)) ;
		
		//Format results and return
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);		
		Double minuteAdd = time / (60*1000);
		cal.add(Calendar.MINUTE, minuteAdd.intValue());
		date = cal.getTime();
		//logger.info("expTime.localTime " + expTime.localTime + "expTime.cloudTime " + expTime.cloudTime + " time " + time + " date " + date.toString());
		SBRAEstimate result = new SBRAEstimate(cost, date, localNodes, cloudNodes);
		
		return result;
	}
	
	private static SBRABalance balance(int jobs, long availableTime, long jobcomplexity, float budget) throws SBRAException{	
		return balance(jobs, availableTime, jobcomplexity, budget, "Amazon");
	}	

	//Compute balance and prioritize due date on balance loop
	private static SBRABalance balance(int jobs, long availableTime, long jobcomplexity, float budget, String system) throws SBRAException{	
		//Try to finish experiment on time using only local nodes
		int localBalance = jobs;
		int cloudBalance = 0;
		int cloudBalanceNodes = 0;
		long compTime = Long.MAX_VALUE;	
		float cost = 0;		
		//Adjust computation time adding cloud nodes until problem matches or we run out of cloud nodes
		while ((availableTime < compTime) && (cloudBalance != jobs)  && (cost <= budget)) {
			
			//Estimate cost and computing time			
			ExperimentTime expTime = computeExperimentTimeSequential(localBalance, cloudBalance, jobcomplexity, availableTime);			
			compTime = (long) Math.max(expTime.localTime, expTime.cloudTime);						

			
			//Take into account cloud time chunks
			if (cloudBalance > 0) {								
				cloudBalanceNodes = (int) (Math.ceil( (cloudBalance * ((double)jobcomplexity)) / getCloudJobChunk(system)));				
			}			
			cost = (float) ((SBRAConstants.LOCAL_JOB_COST * localBalance) + (getCost(system) * cloudBalanceNodes));
			if (jobcomplexity > getCloudJobChunk(system)){
				cost = (float) ((localBalance * SBRAConstants.LOCAL_JOB_COST) + ( cloudBalance * getCost(system)));	
			}
			//Logging for debug			
			//logger.debug("Loop choice localbalance : '" + localBalance + "' cloudbalance : '" + cloudBalance + "' compTime : '" + compTime  + "' availableTime : '" + availableTime  + "'" + "' cost : '" + cost  + "'" + "' cloudBalanceNodes : '" + cloudBalanceNodes  + "'" + "' jobcomplexity : '" + jobcomplexity  + "'" + "' budget : '" + budget  + "'");
			
			if ((availableTime < compTime)  && (cost <= budget) ) {
				localBalance--;
				cloudBalance++;						
			}
		}
		
		//Adjust cost after loop as long as there is, at least one cloud node
		if ((cost > budget) && (cloudBalance > 0)) {
			localBalance++;
			cloudBalance--;
			cost = (float) (cost - getCost(system));
		}
		//logger.debug("Final choie localbalance : '" + localBalance + "' cloudbalance : '" + cloudBalance + "' compTime : '" + compTime  + "' availableTime : '" + availableTime  + "'" + "' cost : '" + cost  + "'" + "' cloudBalanceNodes : '" + cloudBalanceNodes  + "'" + "' jobcomplexity : '" + jobcomplexity  + "'" + "' budget : '" + budget  + "'");		
		
		return new SBRABalance(localBalance, cloudBalance);
	}
	
	public static ExperimentTime computeExperimentTime(int localJobs, int cloudJobs, long jobcomplexity, long availableTime, Distribution method) throws SBRAException{
		if (method == Distribution.SEQUENTIAL){
			return computeExperimentTimeSequential(localJobs, cloudJobs, jobcomplexity, availableTime);
		}
		else{
			throw new SBRAException("Parallel method not suported");			
		}		
	}
	
	private static ExperimentTime computeExperimentTimeSequential(int localJobs, int cloudJobs, long jobcomplexity, long availableTime) {
		return computeExperimentTimeSequential(localJobs, cloudJobs, jobcomplexity, availableTime, "Amazon");
	}
	
	private static ExperimentTime computeExperimentTimeSequential(int localJobs, int cloudJobs, long jobcomplexity, long availableTime, String system) {
		long expComplexity = (long)(localJobs * jobcomplexity);		
		long localTime = (long)(expComplexity/ (SBRAConstants.LOCAL_JOB_POWER * SBRAConstants.LOCAL_NODES));
		//logger.debug("localJobs " + localJobs + "jobcomplexity " + jobcomplexity + "expComplexity " + expComplexity + "localTime " + localTime + "divisor " + (SBRAConstants.LOCAL_JOB_POWER * SBRAConstants.LOCAL_NODES));
		localTime = localTime * 1000;		
		Vector<SequentialInstanceSet> instanceConfiguration = distributeJobsInSequentialWithOverhead(cloudJobs, (int)getCloudJobChunk(system), availableTime, system);		
		double maxSteps = 0;		
		for(SequentialInstanceSet s: instanceConfiguration) {
			if (s.hours > maxSteps) {
				maxSteps = s.hours;
			}
		}
		
				
		maxSteps = maxSteps * getChargeFraction(system);				
		return new ExperimentTime(localTime,maxSteps);				
	}
}

