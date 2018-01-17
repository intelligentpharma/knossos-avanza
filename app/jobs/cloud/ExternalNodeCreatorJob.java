package jobs.cloud;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jobs.KnossosJob;

import models.Experiment;
import models.ExternalNode;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import play.jobs.Job;
import utils.TemplatedConfiguration;
import utils.cloud.AWSNodesManager;
import utils.cloud.ExternalNodesManager;
import utils.cloud.ExternalNodesManagerException;
import utils.queue.PriorityJobQueue;
import utils.queue.PriorityJobQueueItem;
import utils.scripts.ExternalScript;

public class ExternalNodeCreatorJob extends Job {

	public static final Logger logger = Logger.getLogger(ExternalNodeCreatorJob.class);

	Experiment experiment;
	ExternalScript externalScriptLauncher;
	List<KnossosJob> listOfCloudJobs;
	PriorityJobQueue priorityJobQueue;
	List<PriorityJobQueueItem> queuedItems;
	ExternalNodesManager externalNodesCreator;

	public void setExternalNodesCreator(ExternalNodesManager externalNodesCreator) {
		this.externalNodesCreator = externalNodesCreator;
	}

	public void setQueuedItems(List<PriorityJobQueueItem> queuedItems) {
		this.queuedItems = queuedItems;
	}

	public void setListOfCloudJobs(List<KnossosJob> listOfCloudJobs) {
		this.listOfCloudJobs = listOfCloudJobs;
	}

	public void setPriorityJobQueue(PriorityJobQueue priorityJobQueue) {
		logger.info("1 Queued items: " + priorityJobQueue.getQueuedItems().size());
		this.priorityJobQueue = priorityJobQueue;
		logger.info("2 Queued items: " + this.priorityJobQueue.getQueuedItems().size());
	}

	public void setExperiment(Experiment experiment) {
		this.experiment = experiment;
	}

	public void setExternalScriptLauncher(ExternalScript externalScriptLauncher) {
		this.externalScriptLauncher = externalScriptLauncher;
	}

	private void createCloudQueue(String queueName, Set<ExternalNode> nodes) {
		logger.info("xxx Creating cloud queue " + queueName);

		List<String> ips = new ArrayList<>();
		List<String> names = new ArrayList<>();

		String provider = "";

		for (ExternalNode node : nodes) {
			ips.add(node.getPrivateIpAddress());
			names.add(node.generateNameForSlurmScript());			
			provider = node.getExternalProvider();
		}

		String ipsString = StringUtils.join(ips, ",");
		String namesString = StringUtils.join(names, ",");

		String command = String.format(TemplatedConfiguration.get("create_slurm_queue_with_workers"), ipsString, namesString,
				queueName);
		if (ExternalNodesManager.isGoogle(provider)) {
			command += " -gcloud";
		}
		externalScriptLauncher.launch(command);
	}

	public void doJob() {
		logger.info("Queued items: " + this.priorityJobQueue.getQueuedItems().size());
		// PriorityJobQueueItem priorityJobQueueItem =
		// priorityJobQueue.getQueuedItems().get(0);
		PriorityJobQueueItem priorityJobQueueItem = queuedItems.get(0);
		String cloudQueueName = externalNodesCreator.getQueueName(priorityJobQueueItem.getExperiment());
		int nCloudInstances = experiment.numberOfCloudInstances;
		
		externalNodesCreator.startVPNNode(externalScriptLauncher, experiment.externalNodesProvider);

		try {
			Set<ExternalNode> nodes = externalNodesCreator.createOnDemandInstances(nCloudInstances, experiment, cloudQueueName,
					externalScriptLauncher);

			createCloudQueue(cloudQueueName, nodes);
			
			for (ExternalNode externalNode:nodes) {
				externalNodesCreator.executeSlurmd(externalScriptLauncher, externalNode, "slurmd");
			}
			
			if (externalNodesCreator.areNodesAvailable(externalScriptLauncher, nodes, cloudQueueName)) {
				for (KnossosJob job : listOfCloudJobs) {
					priorityJobQueue.launchJob(cloudQueueName, job);
				}
			} else {
				externalNodesCreator.handleExternalNodeInitError(externalScriptLauncher, experiment, nodes);

				// TODO: Check this function because it increases initialSize
				for (KnossosJob job : listOfCloudJobs) {
					priorityJobQueueItem.addJob(job);
				}
				experiment.addErrorEvent(String.format("Error when launching experiment %s . Can not create cloud AWS instances",
						experiment.name));
			}
		} catch (ExternalNodesManagerException E) {
			experiment.addErrorEvent(String.format("Error when launching experiment %s . %s", experiment.name, E.getMessage()));
		}
	}

}
