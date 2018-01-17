package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;

import play.db.jpa.GenericModel;

@Entity(name = "workflowexperiment")
public class WorkflowExperiment extends GenericModel {

	public static final Logger logger = Logger.getLogger(WorkflowExperiment.class);

	@Id
	@ManyToOne(optional = false)
	public Workflow workflow;

	@ManyToOne(optional = false)
	public ComparisonExperiment experiment;

	@Id
	@Column(nullable = false)
	public Integer index;

	@ManyToOne(optional = false)
	public Filter filter;

	@ManyToOne(optional = false)
	public MoleculeDatabase database;

	public WorkflowExperiment() {
	}

	public WorkflowExperiment(Workflow workflow, ComparisonExperiment experiment, Integer index, Filter filter,
			MoleculeDatabase database) {
		this.workflow = workflow;
		this.experiment = experiment;
		this.index = index;
		this.filter = filter;
		this.database = database;
	}

	public static WorkflowExperiment findByExperimentId(long experimentId) {
		return find("byExperiment_id", experimentId).first();
	}

	@Override
	public String toString() {
		return "[workflow_id=" + workflow.id + ", index=" + index + ", experiment_id=" + experiment.id
				+ ", database_id=" + database.id + "]";
	}

	public boolean isLast() {
		return this.index == workflow.workflowExperiments.size() - 1;
	}

	public WorkflowExperiment getNext() {
//		We had to directly access the database because the array changed its order unexpectedly and the experiment returned wasn't correct
//		This has to be reviewed in the future
		if (!isLast()) {
			// return workflow.workflowExperiments.get(this.index + 1);
			return WorkflowExperiment.findByWorkflowAndIndex(workflow, index + 1);
		}
		return null;
	}

	public static WorkflowExperiment findByWorkflowAndIndex(Workflow w, int index) {
		return find("byWorkflowAndIndex", w, index).first();
	}
}