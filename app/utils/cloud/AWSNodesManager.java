package utils.cloud;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import models.ComparisonExperiment;
import models.Experiment;
import models.ExternalNode;
import org.apache.log4j.Logger;
import utils.TemplatedConfiguration;
import utils.scripts.ExternalScript;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.amazonaws.services.ec2.model.UserData;
import com.amazonaws.util.Base64;
import com.amazonaws.services.ec2.model.ShutdownBehavior;

public class AWSNodesManager extends ExternalNodesManager {
	public static final Logger logger = Logger.getLogger(AWSNodesManager.class);

	// EC2 Instance variables
	private static final String IMAGE_ID = "ami-dc5dfabc";//64445c00
	
	private static final String SUBNET_ID = "subnet-8c79e9e9";
	public static final String PROVIDER_NAME = "Amazon";

	private static final int WAIT_TIME_TO_CHECK_NODES = 120000; //Two minutes

	private static final InstanceType INSTANCE_TYPE = InstanceType.C4Xlarge;

	public AWSNodesManager() {
		provider = PROVIDER_NAME;
		initializationTime = WAIT_TIME_TO_CHECK_NODES;
	}

	public Set<ExternalNode> createOnDemandInstances(int size, Experiment experiment, String queueName,
			ExternalScript externalScript) throws ExternalNodesManagerException {

		AmazonEC2Client ec2client = getEc2Client();
		Set<ExternalNode> runningInstances = new HashSet<ExternalNode>();

		Set<String> pendingInstances = askForNewInstances(size, ec2client, queueName);
		runningInstances = waitForPendingInstances(pendingInstances, ec2client, experiment, externalScript);
		return runningInstances;
	}

	// Deletes DB information associated to cloud instances of a particular
	// experiment. Also remove associate queue in slurm
	public boolean deleteExperimentCloudInformation(ExternalScript externalScriptLauncher, Experiment experiment) {
		// Find cloud instances associated to experiment
		logger.info(String.format("Removing experiment cloud information with experiment id '%d'", experiment.getId()));
		if (experiment.numberOfCloudInstances <= 0) {
			return true;
		}
		AmazonEC2Client ec2client = getEc2Client();
		List<ExternalNode> experimentNodes = ExternalNode.findByExperiment(experiment);
		List<String> experimentNodeIds = new ArrayList<String>();
		boolean result = true;
		for (ExternalNode cloudNode : experimentNodes) {
			cloudNode.delete();
			// New version of remove queue will remove nodes, no need to remove
			// then one by one
			// deleteNodeFromSlurm(externalScriptLauncher, cloudNode);
			experimentNodeIds.add(cloudNode.getName());
			// Shut down amazon instance
			try {
				// Kills instance and waits it to finish, returns false if
				// instance can not be finished
				result = shutdownAWSInstance(cloudNode.getExternalId(), ec2client);
			} catch (InterruptedException e) {
				// Also function will return false if an error is raised
				// when
				// trying to kill the instance
				logger.error("Error on deleteExperimentCloudInformation", e);
				result = false;
			}
		}

		removeQueueFromSlurm(externalScriptLauncher, experiment, experimentNodeIds);		

		if (!result) {
			experiment
					.addWarningEvent(String
							.format("Warning when finishing experiment %s. Can not terminate cloud AWS instances. Support information succesfully removed. AWS instances will terminate themselves",
									experiment.name));
		}

		return result;
	}	

	private AmazonEC2Client getEc2Client() {
		AWSCredentials credentials = new ProfileCredentialsProvider("rael").getCredentials();
		Region region = Region.getRegion(Regions.US_WEST_2);
		AmazonEC2Client ec2client = new AmazonEC2Client(credentials);
		ec2client.setRegion(region);

		return ec2client;
	}

	private boolean shutdownAWSInstance(String instanceId, AmazonEC2Client ec2client) throws InterruptedException {
		TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
		List<String> instancesIds = new ArrayList<String>(1);
		instancesIds.add(instanceId);
		terminateInstancesRequest.withInstanceIds(instancesIds);
		ec2client.terminateInstances(terminateInstancesRequest);
		// TODO: Check result of ec2client.terminateInstances?
		Set<String> deletedInstancesIds = waitForTerminatedInstances(instancesIds, ec2client);
		return (deletedInstancesIds.size() == instancesIds.size());
	}

	private Set<String> waitForTerminatedInstances(List<String> instancesIds, AmazonEC2Client ec2client)
			throws InterruptedException {
		Set<String> terminatedInstancesIds = new HashSet<String>();
		List<String> pendingInstancesIds = new ArrayList<String>(instancesIds);

		while (pendingInstancesIds.size() > 0) {
			logger.info("*** Waiting ***");
			Thread.sleep(1000);
			DescribeInstancesResult r = ec2client.describeInstances();
			Iterator<Reservation> ir = r.getReservations().iterator();
			while (ir.hasNext()) {
				Reservation reservation = ir.next();
				List<Instance> instances = reservation.getInstances();
				for (Instance instance : instances) {
					if (instance.getState().getName().equals("terminated")
							&& pendingInstancesIds.contains(instance.getInstanceId())) {
						pendingInstancesIds.remove(instance.getInstanceId());
						terminatedInstancesIds.add(instance.getInstanceId());
					}
				}
			}
		}
		return terminatedInstancesIds;
	}

	

	private void addTags(Instance instance, AmazonEC2Client ec2client, Experiment experiment) {
		logger.info("Creating Tags for New Instances");
		CreateTagsRequest crt = new CreateTagsRequest();
		ArrayList<Tag> arrTag = new ArrayList<Tag>();
		arrTag.add(new Tag().withKey("Name").withValue(getNewInstanceName()));
		arrTag.add(new Tag().withKey("Project").withValue("Avanza"));
		arrTag.add(new Tag().withKey("Experiment_id").withValue(experiment.getId().toString()));
		crt.setTags(arrTag);

		Set<String> instanceIds = new HashSet<>();
		instanceIds.add(instance.getInstanceId());

		crt.setResources(instanceIds);
		ec2client.createTags(crt);
	}

	private Set<String> askForNewInstances(int size, AmazonEC2Client ec2client, String queueName)
			throws ExternalNodesManagerException {
		RunInstancesRequest rir = new RunInstancesRequest();
		rir.withImageId(IMAGE_ID);
		rir.withInstanceType(INSTANCE_TYPE);
		rir.withMinCount(1);
		rir.withMaxCount(size);
		rir.withMonitoring(true);
		rir.withSubnetId(SUBNET_ID);
		rir.withInstanceInitiatedShutdownBehavior(ShutdownBehavior.Terminate);

		// Read user data from userData.conf file
		String userData = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader("./conf/userData.conf"));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			userData = sb.toString();
		} catch (Exception e) {
			logger.info("Error reading userData file" + e.getMessage());
			throw new ExternalNodesManagerException("Error reading userData file" + e.getMessage());
		}
		// Include queue name in user data
		userData = userData.replace("<-QUEUE_VARIABLE->", queueName);
		// Encode
		String encodedUserData = new String(Base64.encode(userData.getBytes()));
		rir.setUserData(encodedUserData);

		ec2client.runInstances(rir);

		// / Find newly created instance id
		Set<String> pendingInstancesIds = new HashSet<>();
		DescribeInstancesResult result = ec2client.describeInstances();
		Iterator<Reservation> i = result.getReservations().iterator();
		while (i.hasNext()) {
			Reservation r = i.next();
			List<Instance> instances = r.getInstances();
			for (Instance instance : instances) {
				if (instance.getState().getName().equals("pending")) {
					pendingInstancesIds.add(instance.getInstanceId());
				}
			}
		}
		return pendingInstancesIds;
	}

	// TODO: rename to something that reflects what the method is actually doing
	private Set<ExternalNode> waitForPendingInstances(Set<String> pendingInstances, AmazonEC2Client ec2client,
			Experiment experiment, ExternalScript externalScriptLauncher) {
		Set<ExternalNode> runningNodes = new HashSet<>();
		int wait_limit = 101;
		while (pendingInstances.size() > 0 && wait_limit > 0) {
			wait_limit--;
			logger.info("*** Waiting ***");

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Nothing to do here, just wait for the next iteration.
			}

			DescribeInstancesResult r = ec2client.describeInstances();
			Iterator<Reservation> it = r.getReservations().iterator();
			while (it.hasNext()) {
				Reservation reservation = it.next();
				List<Instance> instances = reservation.getInstances();

				for (Instance instance : instances) {
					if (instance.getState().getName().equals("running") && pendingInstances.contains(instance.getInstanceId())) {
						ExternalNode externalNode = createNewNode(instance, experiment);
						logger.info("************Insert node into DB*************");
						externalNode.save();

						addTags(instance, ec2client, experiment);
						pendingInstances.remove(instance.getInstanceId());
						runningNodes.add(externalNode);
					}
				}
			}
		}

		return runningNodes;
	}

	private ExternalNode createNewNode(Instance instance, Experiment experiment) {
		ExternalNode externalNode = new ExternalNode();
		externalNode.setExperiment(experiment);
		externalNode.setIpAddress(instance.getPublicIpAddress());
		externalNode.setExternalId(instance.getInstanceId());
		externalNode.setExternalProvider(provider);
		externalNode.setStatus(instance.getState().getName());
		externalNode.setPrivateDns(instance.getPrivateDnsName());
		externalNode.setPrivateIpAddress(instance.getPrivateIpAddress());

		return externalNode;
	}

	

		


	protected String getNewInstanceName() {
		return APPLICATION_NAME + "-" + (new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss")).format(new Date());
	}

	@Override
	public boolean isAmazon() {
		return true;
	}

	@Override
	public boolean isGoogle() {
		return false;
	}

	@Override
	public boolean isAzure() {
		return false;
	}

	@Override
	public void executeSlurmd(ExternalScript externalScript, ExternalNode externalNode, String scriptPath) {
		// TODO Auto-generated method stub
		
	}

}
