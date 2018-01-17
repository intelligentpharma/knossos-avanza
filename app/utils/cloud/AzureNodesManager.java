package utils.cloud;

import java.util.Set;

import org.apache.log4j.Logger;

import models.Experiment;
import models.ExternalNode;
import utils.scripts.ExternalScript;

public class AzureNodesManager extends ExternalNodesManager {
	public static final Logger logger = Logger.getLogger(AzureNodesManager.class);

	public static final String PROVIDER_NAME = "Azure";

	private static final int WAIT_TIME_TO_CHECK_NODES = 120000; // Two minutes

	public AzureNodesManager() {
		provider = PROVIDER_NAME;
		initializationTime = WAIT_TIME_TO_CHECK_NODES;
	}

	@Override
	public Set<ExternalNode> createOnDemandInstances(int size, Experiment experiment, String queueName,
			ExternalScript externalScript) throws ExternalNodesManagerException {
		throw new UnsupportedOperationException("Unsupported external provider");
	}

	@Override
	public boolean deleteExperimentCloudInformation(ExternalScript externalScriptLauncher, Experiment experiment) {
		throw new UnsupportedOperationException("Unsupported external provider");
	}

	@Override
	protected String getNewInstanceName() {
		throw new UnsupportedOperationException("Unsupported external provider");
	}

	@Override
	public boolean isAmazon() {
		return false;
	}

	@Override
	public boolean isGoogle() {
		return false;
	}

	@Override
	public boolean isAzure() {
		return true;
	}

	@Override
	public void executeSlurmd(ExternalScript externalScript, ExternalNode externalNode, String scriptPath) {
		throw new UnsupportedOperationException("Unsupported external provider");

	}

}
