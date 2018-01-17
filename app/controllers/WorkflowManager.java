package controllers;

import java.util.List;

import org.apache.log4j.Logger;

import jobs.comparison.ExperimentLauncher;
import models.AlignmentBox;
import models.ComparisonExperiment;
import models.ExperimentStatus;
import models.Filter;
import models.MoleculeDatabase;
import models.User;
import models.Workflow;
import models.WorkflowExperiment;
import utils.Access;
import utils.Access.Level;
import utils.Factory;

public class WorkflowManager extends KnossosController {
	public static final Logger logger = Logger.getLogger(WorkflowManager.class);

	public static void addStep(Long workflowId, String workflowName, String experimentName, long targetDatabaseId,
			long probeDatabaseId, int engineId, int chargeType, int similarityCalculationType, AlignmentBox box,
			int numRuns, int energyEvaluations, int exhaustiveness, String boxSelectionType, long referenceDatabaseId,
			String username, String vinaBinaryType, int searchDepth, long flex, boolean score_only, boolean local_only,
			boolean randomize_only, String seed, String num_modes, String energy_range, String addition, String spacing,
			String smooth, String dielectric, String tran0, String quat0, String dihe0, String tstep, String qstep,
			String dstep, String torsdof, String rmstol, String extnrg, String e0max, String ga_pop_size,
			String ga_num_generations, boolean epdb, boolean include_1_4_interactions, boolean analysis,
			String algorithm, Integer numberOfCloudJobs, Integer numberOfCloudInstances, String provider,
			String filterType, Float filterThreshold, Boolean decreasing, float similarity, float activity, String descriptor, Long modelId,
			String chemblVersion) {				
		User user = User.findByUserName(username);
		Workflow workflow = null;
		int nextIndex = 0;

		if (workflowId != null) {
			workflow = Workflow.findById(workflowId);
			nextIndex = workflow.workflowExperiments.size();
		} else {
			workflow = new Workflow(workflowName, user);
			workflow.save();
		}

		MoleculeDatabase db = new MoleculeDatabase(workflowName + "-" + nextIndex, user);
		db.save();
		
		Filter filter = new Filter(filterType, filterThreshold, user, decreasing);
		filter.save();

		final boolean launchOnSave = false;
		
		if (engineId == Factory.PYTHIA || engineId == Factory.MEDEA_WF) {
			targetDatabaseId = probeDatabaseId;
		}
		ComparisonExperiment exp = ExperimentManager.experimentFromVars(experimentName, targetDatabaseId, probeDatabaseId, engineId,
				chargeType, similarityCalculationType, box, numRuns, energyEvaluations, exhaustiveness,
				boxSelectionType, referenceDatabaseId, username, vinaBinaryType, searchDepth, flex, score_only,
				local_only, randomize_only, seed, num_modes, energy_range, addition, spacing, smooth, dielectric, tran0,
				quat0, dihe0, tstep, qstep, dstep, torsdof, rmstol, extnrg, e0max, ga_pop_size, ga_num_generations,
				epdb, include_1_4_interactions, analysis, algorithm, numberOfCloudJobs, numberOfCloudInstances,
				provider, similarity, activity, descriptor, chemblVersion, modelId, launchOnSave);

		exp.status = ExperimentStatus.WAITING;
		exp.owner = user;
		exp.save();

		WorkflowExperiment step = new WorkflowExperiment(workflow, exp, nextIndex, filter, db);
		step.save();

		workflow.workflowExperiments.add(step);
		workflow.save();

		render(step);
	}

	public static void getWorkflow(long workflowId) {
		logger.info(String.format("getWorkflow: %d", workflowId));
		Workflow workflow = Workflow.findById(workflowId);
		render(workflow);
	}

	@Access(level = Level.READ)
	public static void listWorkflowsOwnedBy(String username) {
		User user = User.findByUserName(username);
		List<Workflow> workflows = Workflow.findAllOwnedBy(user);
		render(workflows);
	}

	public static void runWorkflow(Long id, String name, String username) {
		logger.info(String.format("Running workflow [%d] %s", id, name));
		Workflow workflow = Workflow.findById(id);
		ComparisonExperiment firstExperiment = workflow.workflowExperiments.get(0).experiment;
		firstExperiment.status = ExperimentStatus.QUEUED;
		firstExperiment.save();

		// The code below is identical to that in ExperimentManager.runExperiment
		// Calling directly that method results in an excessive garbage collection which
		// results in a heap space error. Duplicated the code to avoid that.
		Factory factory = getFactory(username);
		ExperimentLauncher launch = factory.createExperimentLauncher(firstExperiment);
		launch.in(10);
		logger.info(String.format(
				"Persistent Experiment with name %s added by %s with cloud nodes '%s' and cloud jobs '%s' ",
				firstExperiment.name, username, firstExperiment.numberOfCloudInstances,
				firstExperiment.numberOfCloudJobs));
		firstExperiment.addSeenEvent("Experiment " + firstExperiment.name + " added");
	}

	@Access(level = Level.MODIFY)
	public static void deleteWorkflow(long workflowId) {
		logger.info(String.format("Erasing workflow %d start: ", workflowId));
		Workflow workflow = Workflow.findById(workflowId);
		// Get list of workflowExperiments
		List<WorkflowExperiment> weList = workflow.workflowExperiments;
		// Delete the workflow
		workflow.delete();
		// Go over workflowExperiment entries to delete associated tables
		for (WorkflowExperiment we : weList) {
			logger.debug(String.format("Removing associated elements to worflowExperiment with index: %d", we.index));
			// Remove comparisonexperiment associated to workflowExperiment
			ComparisonExperiment exp = we.experiment;
			exp.delete();
			logger.debug(String.format("Removing associated experiment: %d", exp.id));
			// Remove database associated to workflowExperiment
			MoleculeDatabase db = we.database;
			db.removeMoleculeDatabaseThroughSql();
			logger.debug(String.format("Removing associated database: %d", db.id));
			// Remove filter associated to workflowExperiment
			Filter ft = we.filter;
			ft.delete();
			logger.debug(String.format("Removing associated filter: %d", ft.id));

		}
		logger.info(String.format("Erasing workflow %d end: ", workflowId));
	}

}