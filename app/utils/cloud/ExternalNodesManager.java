package utils.cloud;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import utils.TemplatedConfiguration;
import utils.scripts.ExternalScript;
import models.ComparisonExperiment;
import models.Experiment;
import models.ExperimentStatus;
import models.ExternalNode;

public abstract class ExternalNodesManager {

	public static final Logger logger = Logger.getLogger(ExternalNodesManager.class);

	protected static int initializationTime;

	protected static String provider;

	protected static final String APPLICATION_NAME = "avanza";

	// Builds a queue name
	public String getQueueName(Experiment experiment) {
		return experiment.getId().toString();
	}

	public abstract Set<ExternalNode> createOnDemandInstances(int size, Experiment experiment, String queueName,
			ExternalScript externalScript) throws ExternalNodesManagerException;

	public abstract boolean deleteExperimentCloudInformation(ExternalScript externalScriptLauncher,
			Experiment experiment);

	protected abstract String getNewInstanceName();

	public abstract boolean isAmazon();

	public abstract boolean isGoogle();

	public abstract boolean isAzure();
	
	public abstract void executeSlurmd (ExternalScript externalScript, ExternalNode externalNode, String scriptPath);

	// Handles error on intializing AWS instance. Cleans DB information (nodes)
	// and associated slurm queues. Adds error to experiment
	public void handleExternalNodeInitError(ExternalScript externalScriptLauncher, Experiment experiment,
			Set<ExternalNode> nodes) {
		logger.info("handleAWSInitError start");

		String cloudQueueName = String.valueOf(experiment.getId());

		List<String> experimentNodeIds = new ArrayList<String>();
		for (ExternalNode cloudNode : nodes) {
			// Remove node DB Information
			logger.info(String.format("Removing node from database '%s'", cloudQueueName, cloudNode.getName()));
			cloudNode.delete();
			experimentNodeIds.add(cloudNode.getName());
		}

		String experimentNodeIdsCmd = StringUtils.join(experimentNodeIds, ",");
		logger.info(
				String.format("Removing queue from slurm queue '%s' nodes '%s'", cloudQueueName, experimentNodeIdsCmd));
		removeQueueFromSlurm(externalScriptLauncher, experiment, experimentNodeIds);

		logger.info("handleAWSInitError finish");
	}

	public static String getProviderNameFromExperiment(Experiment experiment) {
		List<ExternalNode> externalNodes = ExternalNode.findByExperiment(experiment);
		if (externalNodes != null && !externalNodes.isEmpty()) {
			return externalNodes.get(0).getExternalProvider();
		} else {
			return AWSNodesManager.PROVIDER_NAME;
		}
	}

	public static boolean isAmazon(String providerName) {
		return providerName.equalsIgnoreCase(AWSNodesManager.PROVIDER_NAME);
	}

	public static boolean isGoogle(String providerName) {
		return providerName.equalsIgnoreCase(GcloudNodesManager.PROVIDER_NAME);
	}

	public static boolean isAzure(String providerName) {
		return providerName.equalsIgnoreCase(AzureNodesManager.PROVIDER_NAME);
	}

	private boolean areNodesAvailableForSsh(Set<ExternalNode> nodes, ExternalScript externalScriptLauncher)
			throws Exception {
		try {
			for (ExternalNode node : nodes) {
				logger.info("****** Checking ssh connection for node: " + node.getPublicDns() + " ********");
				String command = String.format(TemplatedConfiguration.get("check_connection"), node.getAddressForSsh());
				externalScriptLauncher.launch(command);
			}
		} catch (Exception e) {
			if (e.getCause().getMessage().contains("Connection refused")) {
				logger.info("****** Waiting for node to accept connections ********");
				return false;
			} else {
				logger.info("****** Slurm script failed **********");
				throw new Exception();
			}
		}
		return true;
	}

	private boolean areNodesAvailableForSlurm(ExternalScript externalScriptLauncher, String output, Set<ExternalNode> nodes, String cloudQueueName) {
		Scanner scanner = new Scanner(output);
		int availableNodes = 0;
		try {
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.startsWith(cloudQueueName)) {
					if (line.contains("idle*")) {
						return false;
					}else if(line.contains("drain")) {
						for (ExternalNode node : nodes) {
							if (line.contains(node.getNameInSlurm()) && line.contains("drain")) {
								String slurmScriptCommand = String.format(TemplatedConfiguration.get("undrain"), node.getNameInSlurm());
								externalScriptLauncher.launch(slurmScriptCommand);								
							}
						}
						return false;
					}else {
						for (ExternalNode node : nodes) {
							if (line.contains(node.getNameInSlurm()) && line.contains("idle")) {
								availableNodes = parseSinfoNumberOfNodes(line);
								break;
							}
						}
					}
				}
			}
		} finally {
			scanner.close();
		}

		return availableNodes == nodes.size();
	}

	// Extracts number of nodes from sinfo line
	private int parseSinfoNumberOfNodes(String line) {
		// Get everything before idle
		line = line.split("idle")[0];
		// Get everything after :DD
		line = line.split(":[0-9]+")[line.split(":[0-9]+").length - 1];
		// Trim in case some spaces got through, transform to int and return
		line = line.trim();
		return Integer.parseInt(line);
	}

	public boolean areNodesAvailable(ExternalScript externalScriptLauncher, Set<ExternalNode> nodes,
			String cloudQueueName) {

		logger.info("********** Wait time to check nodes ********");
		try {
			Thread.sleep(initializationTime);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int loops = 150;
		String command = "";

		while (loops > 0) {
			try {
				Thread.sleep(5000);
				loops--;
				command = String.format(TemplatedConfiguration.get("sinfo"));
				String output = externalScriptLauncher.launch(command);
				if (areNodesAvailableForSsh(nodes, externalScriptLauncher)) {
					if (areNodesAvailableForSlurm(externalScriptLauncher, output, nodes, cloudQueueName)) {
						return true;
					}
				}
			} catch (Exception e) {
				logger.info(String.format(String.format("****** Script failed '%s' **********", e.getMessage())));
				return false;
			}
		}

		return false;
	}

	protected boolean removeQueueFromSlurm(ExternalScript externalScriptLauncher, Experiment experiment,
			List<String> experimentNodeIds) {
		// Delete queue associated to experiment from slurm
		if (experimentNodeIds.size() > 0) {
			String queueName = getQueueName(experiment);
			String experimentNodeIdsCmd = StringUtils.join(experimentNodeIds, ",");

			logger.info(String.format("Removing queue from slurm queue %s nodes %s", queueName, experimentNodeIdsCmd));

			String slurmScriptCommand = String.format(TemplatedConfiguration.get("remove_hidra_queue"), queueName,
					experimentNodeIdsCmd);
			String output = externalScriptLauncher.launch(slurmScriptCommand);
			return output.equalsIgnoreCase("SUCCESS");
		}
		return false;
	}

	public void startVPNNode(ExternalScript externalScriptLauncher, String externalProvider) {
		String command = String.format(TemplatedConfiguration.get("launch_vpn"));
		if (isGoogle(externalProvider)) {
			command += " -gcloud";
		}
		externalScriptLauncher.launch(command);
	}

	public boolean areExperimentsPending(String provider) {
		List<ComparisonExperiment> pendingExperiments = ComparisonExperiment.findByProviderAndNotFinished(provider);
		return !pendingExperiments.isEmpty();
	}

	public void stopVPNNode(ExternalScript externalScriptLauncher, String externalProvider) {
		if (!areExperimentsPending(externalProvider)) {
			String command = String.format(TemplatedConfiguration.get("stop_vpn"));
			if (isGoogle(externalProvider)) {
				command += " -gcloud";
			}else if(isAmazon(externalProvider)){
				command += " -aws";
			}
			externalScriptLauncher.launch(command);
		}else {
			logger.info("Won't stop vpn node, there are other experiments executing");
		}
	}

}
