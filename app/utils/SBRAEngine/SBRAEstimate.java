package utils.SBRAEngine;

import java.util.Date;

//Time and delivery date of SBRA Experimnent
public class SBRAEstimate {
	private double cost;
	private Date delivery;
	private int localNodes;
	private int cloudNodes;
	
	public SBRAEstimate(double cost, Date delivery, int localNodes, int cloudNodes){
		this.cost = cost;
		this.delivery = delivery;
		this.localNodes = localNodes;
		this.cloudNodes = cloudNodes;
	}
	
	public double getCost(){
		return cost;
	}
	
	public Date getDelivery(){
		return delivery;
	}
	
	public int getLocalNodes(){
		return localNodes;
	}
	
	public int getCloudNodes(){
		return cloudNodes;
	}
}
