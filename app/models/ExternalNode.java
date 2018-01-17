package models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import play.db.jpa.Model;
import utils.cloud.ExternalNodesManager;

@Entity
public class ExternalNode extends Model {

	@Column
	String ipAddress;
	
	@Column
	String privateIpAddress;
	
	@Column
	String name;
	
	@Column
	String externalId;
	
	@Column
	String externalProvider;
	
	@Column
	String status;		

	@Column
	String privateDns;
	
	@ManyToOne(optional = false)
	ComparisonExperiment experiment;
	
	public String getPublicDns(){
		String dns = ipAddress.replace(".", "-");
		dns = "ec2-" + dns + ".us-west-2.compute.amazonaws.com";
		return dns;
	}


	public String getName() {		
		return name;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getExternalProvider() {
		return externalProvider;
	}

	public void setExternalProvider(String externalProvider) {
		this.externalProvider = externalProvider;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIpAddress() {
		return ipAddress;
	}
	
	
	public String getPrivateIpAddress() {
		return privateIpAddress;
	}

	public void setPrivateIpAddress(String privateIpAddress) {
		this.privateIpAddress = privateIpAddress;
		this.name=getInstanceName(privateIpAddress);
	}

	public String getPrivateDns() {
		return privateDns;
	}

	public void setPrivateDns(String dns) {
		this.privateDns = dns;
	}
	
	public Experiment getExperiment() {
		return experiment;
	}

	public void setExperiment(Experiment experiment) {
		if (! (experiment instanceof ComparisonExperiment)) {
			throw new UnsupportedOperationException("Only ComparisonExperiment supported at the moment.");
		}
		
		this.experiment = (ComparisonExperiment) experiment;
	}
	
	public static ExternalNode findByExternalId(String externalId) {
		return find("byExternalId", externalId).first();
	}
	
	public static List<ExternalNode> findByExperiment(Experiment experiment) {
		return find("byExperiment", experiment).fetch();
	}
	
	
	private String getInstanceName(String privateIpAddress) {
		String str = privateIpAddress.replace(".", "-");		
		return "ip-" + str;
	}

	public void setIpAddress(String publicIpAddress) {
		this.ipAddress = publicIpAddress;		
		
	}

	public String generateNameForSlurmScript() {
		
		if (ExternalNodesManager.isAmazon(externalProvider)){			
			return privateDns.split("\\.")[0];
		}else{
			return privateDns;
		}		
	}


	public String getNameInSlurm() {
		
		if (ExternalNodesManager.isAmazon(externalProvider)){			
			return name;
		}else{
			return privateDns;
		}
	}


	public Object getAddressForSsh() {
		if (ExternalNodesManager.isAmazon(externalProvider)){			
			return getPublicDns();
		}else{
			return getIpAddress();
		}
	}
	
}
