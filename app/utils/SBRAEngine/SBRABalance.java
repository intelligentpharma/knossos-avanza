package utils.SBRAEngine;

public class SBRABalance {
	private int localJobs;
	private int cloudJobs;
	
	public SBRABalance(int local, int cloud){
		this.localJobs = local;
		this.cloudJobs = cloud;
		
	}
	
	public int getLocalJobs(){
		return localJobs;
	}
	
	public int getCloudJobs(){
		return cloudJobs;
	}		
}
