package utils.SBRAEngine;

public interface SBRAConstants {
	//Number of easy jobs per second on a node
	public double LOCAL_JOB_POWER = 1;
	public double AMAZON_CLOUD_JOB_POWER = 1.67;
	public double GOOGLE_CLOUD_JOB_POWER = 2;
	public double AZURE_CLOUD_JOB_POWER = 1;
	
	//AMAZON Cloud machines are charged in fractions of one hour
	public int AMAZON_INSTANCE_CHARGE_FRACTION = 60*60;
	//GOOGLE Cloud machines are charged in fractions of one minute
	public int GOOGLE_INSTANCE_CHARGE_FRACTION = 60*60;
	//GOOGLE Cloud machines are charged in fractions of one minute
	public int AZURE_INSTANCE_CHARGE_FRACTION = 60*60;
	
	//AMAZON Cloud machines take 8 minutes to boot
	public int AMAZON_INSTANCE_BOOT_TIME = 8*60;
	//GOOGLE Cloud machines take 4 minutes to boot
	public int GOOGLE_INSTANCE_BOOT_TIME = 8*60;
	//AZURE Cloud machines take 4 minutes to boot
	public int AZURE_INSTANCE_BOOT_TIME = 8*60;
	
	//Number of available local nodes
	public int LOCAL_NODES = 10;	
	
	//Cost per job
	public double LOCAL_JOB_COST = 0;
	//public double CLOUD_CHUNK_COST = 50;
	public double GOOGLE_CHUNK_COST = 0.398;
	public double AMAZON_CHUNK_COST = 0.199;
	public double AZURE_CHUNK_COST = 0.0995;
	
	//On some systems are to be executed in chunks (i.e., amazon charges hourly so we should distribute jobs hourly)
	public int LOCAL_JOB_CHUNK = LOCAL_NODES;//(int) Math.floor(LOCAL_NODES / LOCAL_JOB_POWER);//LOCAL_NODES * (int) Math.floor(CLOUD_INSTANCE_CHARGE_FRACTION / LOCAL_JOB_POWER);
	//public int CLOUD_JOB_CHUNK = (int) Math.floor(CLOUD_INSTANCE_CHARGE_FRACTION * CLOUD_JOB_POWER);
	
	public int AMAZON_CLOUD_JOB_CHUNK = (int) Math.floor(AMAZON_INSTANCE_CHARGE_FRACTION * AMAZON_CLOUD_JOB_POWER);
	public int GOOGLE_CLOUD_JOB_CHUNK = (int) Math.floor(GOOGLE_INSTANCE_CHARGE_FRACTION * GOOGLE_CLOUD_JOB_POWER);
	public int AZURE_CLOUD_JOB_CHUNK = (int) Math.floor(AZURE_INSTANCE_CHARGE_FRACTION * AZURE_CLOUD_JOB_POWER);
	
	//Priorization by due-date or by cost
	public enum Priorization {COST, DATE};
	public Priorization PRIORIZATION_METHOD = Priorization.COST;	
	
	//Cloud parallel or sequential distribution priority
	public enum Distribution {PARALLEL, SEQUENTIAL};
	public Distribution DISTRIBUTION_METHOD = Distribution.PARALLEL;
	
	//For the tests
	public double CLOUD_JOB_POWER = 10;
	public int CLOUD_INSTANCE_CHARGE_FRACTION = 60*60;
	public int CLOUD_INSTANCE_BOOT_TIME = 8*60;
	public double CLOUD_CHUNK_COST = 50;
	public int CLOUD_JOB_CHUNK = (int) Math.floor(CLOUD_INSTANCE_CHARGE_FRACTION * CLOUD_JOB_POWER);
}
