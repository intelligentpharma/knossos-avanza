package controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.hibernate.StatelessSession;

import files.DatabaseFiles;
import files.FileFormatTranslator;
import files.FileGenerator;
import files.FileUtilsImpl;
import files.formats.pdbqt.PdbClusterDataExtractor;
import jobs.comparison.ExperimentLauncher;
import jobs.comparison.MapsGeneratorJob;
import jobs.comparison.MultipleExperimentRemovalJob;
import jobs.comparison.PholusLaunch;
import jobs.database.crud.MoleculeDatabaseFromExperimentJob;
import models.Alignment;
import models.AlignmentBox;
import models.ComparisonExperiment;
import models.ComparisonExperimentGuest;
import models.Deployment;
import models.ExperimentStatus;
import models.MapsSimilarities;
import models.MoleculeDatabase;
import models.Pharmacophore;
import models.PharmacophoreKnossos;
import models.PharmacophoreSchrodinger;
import models.Ponderation;
import models.Scoring;
import models.User;
import models.Workflow;
import models.WorkflowExperiment;
import play.jobs.Job;
import play.mvc.Before;
import utils.Access;
import utils.Access.Level;
import utils.DBUtilsImpl;
import utils.Factory;
import utils.TemplatedConfiguration;
import utils.SBRAEngine.SBRABalance;
import utils.SBRAEngine.SBRACore;
import utils.SBRAEngine.SBRAEstimate;
import utils.SBRAEngine.SBRAException;
import utils.cloud.ExternalNodesManager;
import utils.complexityEstimation.ComplexityEstimationCore;
import utils.experiment.AbstractExperimentExporter;
import utils.experiment.ExperimentImporter;
import utils.pharmacophore.PharmacophoreScoreCalculator;
import utils.queue.PriorityJobQueue;
import utils.queue.PriorityJobQueueItem;
import utils.queue.PriorityJobQueueItemImpl;
import utils.scripts.pholus.Pholus;
import visitors.ExperimentCsvGenerator;
import visitors.StructureDataFileGeneratorOrderedByPonderation;

public class ExperimentManager extends KnossosController {
	public static final Logger logger = Logger.getLogger(ExperimentManager.class);

	

	// Proposes a local-cloud computation balance based on experiment
	// complexity, due date and available budget
	@Access(level = Level.READ)
	public static void getSBRABalance(Long probeDbId, Long targetDbId, Long jobcomplexity, Long duedate, Float budget) {
		logger.info("getSBRABalance " + "probedbid : '" + probeDbId + "', " + "targetdbid : '" + targetDbId + "', "
				+ "jobcomplexity : '" + jobcomplexity + "', " + "duedate : '" + duedate + "', " + "budget : '" + budget
				+ "'");
		HashMap<String, Object> experimentBalance = new HashMap();

		int probeSize = MoleculeDatabaseManager.getDeploymentsNumber(probeDbId.toString(), null);
		int targetSize = MoleculeDatabaseManager.getDeploymentsNumber(targetDbId.toString(), null);

		try {
			SBRABalance expBalanceAmazon = SBRACore.getBalance(probeSize, targetSize, jobcomplexity, duedate, budget,
					"Amazon");
			experimentBalance.put("Amazon", expBalanceAmazon);
			SBRABalance expBalanceGoogle = SBRACore.getBalance(probeSize, targetSize, jobcomplexity, duedate, budget,
					"Google");
			experimentBalance.put("Google", expBalanceGoogle);
			SBRABalance expBalanceAzure = SBRACore.getBalance(probeSize, targetSize, jobcomplexity, duedate, budget,
					"Azure");
			experimentBalance.put("Azure", expBalanceAzure);
		} catch (SBRAException e) {
			e.printStackTrace();
			logger.info(e.getMessage());
			// TODO: Return error to FrontEnd or whoever called the service
		}

		renderJSON(experimentBalance);
	}

	// Estimates cost and time of an experiment based on SBRA planner results,
	// experiment properties and cloud properties
	@Access(level = Level.READ)
	public static void getSBRAEstimation(Long jobcomplexity, Long duedate, int localbalance, int cloudbalance) {
		logger.info("getSBRAEstimation " + "jobcomplexity : '" + jobcomplexity + "'" + "due date : '" + duedate + "'"
				+ "localbalance : '" + localbalance + "'" + "cloudbalance : '" + cloudbalance + "'");

		HashMap<String, Object> experimentEstimation = new HashMap();
		HashMap<String, Object> experimentEstimationSet = new HashMap();
		SBRAEstimate expCostAmazon = SBRACore.getEstimation(jobcomplexity, duedate, localbalance, cloudbalance,
				"Amazon");
		SBRAEstimate expCostGoogle = SBRACore.getEstimation(jobcomplexity, duedate, localbalance, cloudbalance,
				"Google");
		SBRAEstimate expCostAzure = SBRACore.getEstimation(jobcomplexity, duedate, localbalance, cloudbalance, "Azure");

		experimentEstimation = new HashMap();
		Date date = expCostAmazon.getDelivery();
		Double cost = expCostAmazon.getCost();
		Integer cloudNodes = expCostAmazon.getCloudNodes();
		experimentEstimation.put("cost", cost);
		experimentEstimation.put("delivery", date);
		experimentEstimation.put("cloudNodes", cloudNodes);
		experimentEstimationSet.put("Amazon", experimentEstimation);

		experimentEstimation = new HashMap();
		date = expCostGoogle.getDelivery();
		cost = expCostGoogle.getCost();
		cloudNodes = expCostGoogle.getCloudNodes();
		experimentEstimation.put("cost", cost);
		experimentEstimation.put("delivery", date);
		experimentEstimation.put("cloudNodes", cloudNodes);
		experimentEstimationSet.put("Google", experimentEstimation);

		experimentEstimation = new HashMap();
		date = expCostAzure.getDelivery();
		cost = expCostAzure.getCost();
		cloudNodes = expCostAzure.getCloudNodes();
		experimentEstimation.put("cost", cost);
		experimentEstimation.put("delivery", date);
		experimentEstimation.put("cloudNodes", cloudNodes);
		experimentEstimationSet.put("Azure", experimentEstimation);

		// logger.info(expCostAmazon.getCost() + " | " + expCostGoogle.getCost() + " | "
		// + expCostAzure.getCost());

		renderJSON(experimentEstimationSet);
	}

	// Estimate experiment's job complexity based on experiment type and
	// parameter
	@Access(level = Level.READ)
	public static void getComplexityEstimation(String type, Integer parameter) {
		logger.info("getComplexityEstimation " + "type : '" + type + "'" + "parameter : '" + parameter);

		HashMap<String, Object> complexityEstimation = new HashMap();
		Integer estimation = -1;
		try {
			estimation = ComplexityEstimationCore.estimateComplexity(type, parameter);
		} catch (Exception E) {
			logger.info("getComplexityEstimation Error, can not find complexity configuration file");
		}
		complexityEstimation.put("estimation", estimation);
		renderJSON(complexityEstimation);
	}

	

	@Access(level = Level.MODIFY)
	public static void getAllExperimentsForWorkflow(long workflowId) {

		List<ComparisonExperiment> experiments = new ArrayList<ComparisonExperiment>();
		Workflow workflow = Workflow.findById(workflowId);
		List<WorkflowExperiment> wfList = workflow.workflowExperiments;
		for (WorkflowExperiment wf : wfList) {
			experiments.add(wf.experiment);
		}
		render(experiments);
	}	

}