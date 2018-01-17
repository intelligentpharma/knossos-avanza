package jobs.cloud;

import models.Experiment;
import play.jobs.Job;
import utils.cloud.AWSNodesManager;
import utils.cloud.ExternalNodesManager;
import utils.scripts.ExternalScript;

public class ExternalNodeRemoverJob extends Job {

	Experiment experiment;
	ExternalScript externalScriptLauncher;
	ExternalNodesManager externalNodesCreator;

	public void setExternalNodesCreator(ExternalNodesManager externalNodesCreator) {
		this.externalNodesCreator = externalNodesCreator;
	}

	public void setExperiment(Experiment experiment) {
		this.experiment = experiment;
	}

	public void setExternalScriptLauncher(ExternalScript externalScriptLauncher) {
		this.externalScriptLauncher = externalScriptLauncher;
	}

	public void doJob() {
		externalNodesCreator.deleteExperimentCloudInformation(externalScriptLauncher, experiment);
		externalNodesCreator.stopVPNNode(externalScriptLauncher, experiment.externalNodesProvider);
	}

}
