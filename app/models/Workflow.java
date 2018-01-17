package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import json.GsonExclude;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import play.db.jpa.JPA;

@Entity(name="workflow")
public class Workflow extends OwnedModel {
	public static final Logger logger = Logger.getLogger(Workflow.class);
	
	public static final String TYPE = "workflow";

    @Column(nullable = false)
    public String name;    
    
    @GsonExclude
	@OneToMany(mappedBy = "workflow", cascade=CascadeType.ALL)    
    @LazyCollection(LazyCollectionOption.FALSE)
	public List<WorkflowExperiment> workflowExperiments = new ArrayList<WorkflowExperiment>();
    
    public String status;
   
    public Workflow() {
    	
    }
    
    public Workflow(String name, User owner) {
    	this.name = name;
    	this.owner = owner;
    }
    
    public static Workflow findByName(String name) {
    	return find("byName", name).first();
    }   
    
    private void assignStatus(){
    	
    	int size = workflowExperiments.size();
    	status = "Error retrieving status";
    	if (size > 0){    		
    		if (workflowExperiments.get(size-1).experiment.status.equals(ExperimentStatus.FINISHED)){
    			status=ExperimentStatus.FINISHED;
    		}else if (workflowExperiments.get(0).experiment.status.equals(ExperimentStatus.QUEUED)){
    			status=ExperimentStatus.QUEUED;
    		}else if (workflowExperiments.get(0).experiment.status.equals(ExperimentStatus.WAITING)){
    			status=ExperimentStatus.WAITING;
    		}else {
    			status=ExperimentStatus.RUNNING;
    		}    	    	    	
    	}
    }
    
    //Backend for findAllOwnedBy
    private static List<Workflow> findAllOwnedByBack(User user) {    	
    	return Workflow.find("select w from workflow w where w.owner = ? order by w.id desc", user).fetch();
	}
    
    //Enacts typical findAllOwnedBy and assigns status to every element in the list
    public static List<Workflow> findAllOwnedBy(User user) {
    	List<Workflow> wfList = findAllOwnedByBack(user);
    	for (Workflow wf : wfList){
    		wf.assignStatus();
    	}    	    	
    	return wfList;
	}
    
    @Override
	public String getType() {
		return TYPE;
	}
    
    @Override
    public String toString() {
    	return "[id=" + id + ", name=" + name + "]";
    }
}