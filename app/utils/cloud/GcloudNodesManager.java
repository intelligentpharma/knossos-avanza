package utils.cloud;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.Experiment;
import models.ExternalNode;

import org.apache.log4j.Logger;

import utils.TemplatedConfiguration;
import utils.scripts.ExternalScript;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.compute.model.AccessConfig;
import com.google.api.services.compute.model.Disk;
import com.google.api.services.compute.model.DiskList;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.InstanceList;
import com.google.api.services.compute.model.NetworkInterface;
import com.google.api.services.compute.model.Operation;

import files.FileUtils;
import files.FileUtilsImpl;

public class GcloudNodesManager extends ExternalNodesManager {
	public static final Logger logger = Logger.getLogger(GcloudNodesManager.class);

	public static final String PROVIDER_NAME = "Google";

	private static final String PROJECT_ID = "avanza-166710";
	private static final String ZONE_NAME = "us-central1-a";

	private static final String INSTANCE_TYPE = "n1-highcpu-2";

	private static final int WAIT_TIME_TO_CHECK_NODES = 0;

	/** Global instance of the JSON factory. */
	private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	// TODO: change for application.conf
	private static final String STARTUP_SCRIPT = "userData_gcloud.sh";

	private static final String TMP_EXTERNAL_INSTANCE_FOLDER = "/tmp/";

	public GcloudNodesManager() {
		initializationTime = WAIT_TIME_TO_CHECK_NODES;
		provider = PROVIDER_NAME;
	}

	@Override
	public Set<ExternalNode> createOnDemandInstances(int size, Experiment experiment, String queueName,
			ExternalScript externalScript) throws ExternalNodesManagerException {

		HttpTransport httpTransport;
		Set<ExternalNode> runningInstances = new HashSet<ExternalNode>();

		try {
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();

			// Authenticate using Google Application Default Credentials.
			GoogleCredential credential = getCredentials();

			// Create Compute Engine object for listing instances.
			Compute compute = new Compute.Builder(httpTransport, JSON_FACTORY, credential)
					.setApplicationName(APPLICATION_NAME).build();

			// /hidra-01:/root/avanza/conectar_vpn.sh -gcloud

			for (int i = 0; i < size; i++) {
				runningInstances.add(createWorker(compute, getNewInstanceName(), experiment));
			}
			String originScriptPath = TemplatedConfiguration.get("gcloud_userData");
			String destinationScriptPath = TMP_EXTERNAL_INSTANCE_FOLDER + (new File(originScriptPath)).getName();
			for (ExternalNode externalNode : runningInstances) {
				sendScript(externalScript, externalNode, originScriptPath, destinationScriptPath, queueName);
				executeUserData(externalScript, externalNode, destinationScriptPath, queueName);				
			}
		} catch (GeneralSecurityException | IOException | InterruptedException e) {
			throw new ExternalNodesManagerException(e.getMessage());
		}
		return runningInstances;
	}

	private void sendScript(ExternalScript externalScript, ExternalNode externalNode, String originScriptPath,
			String destinationScriptPath, String queueName) throws IOException, InterruptedException {
		// File newFile = replaceQueueName("scripts/" + STARTUP_SCRIPT,
		// queueName);
		copyToRemote(externalScript, externalNode, originScriptPath, destinationScriptPath);
	}

	private File replaceQueueName(String startupScriptPath, String queueName) throws IOException {
		FileUtils fileUtils = new FileUtilsImpl();
		File tmpFile = File.createTempFile(STARTUP_SCRIPT, ".tmp");
		org.apache.commons.io.FileUtils.copyFile(new File(startupScriptPath), tmpFile);
		fileUtils.replaceStringInFile(tmpFile, "<-QUEUE_VARIABLE->", queueName);
		setFilePermissions(tmpFile);
		return tmpFile;
	}

	private void setFilePermissions(File tmpFile) throws IOException {

		Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
		// add owners permission
		perms.add(PosixFilePermission.OWNER_READ);
		perms.add(PosixFilePermission.OWNER_WRITE);
		perms.add(PosixFilePermission.OWNER_EXECUTE);
		// add group permissions
		perms.add(PosixFilePermission.GROUP_READ);
		// add others permissions
		perms.add(PosixFilePermission.OTHERS_READ);

		Files.setPosixFilePermissions(tmpFile.toPath(), perms);
	}

	private static void executeUserData(ExternalScript externalScript, ExternalNode externalNode, String scriptPath,
			String queueName) throws InterruptedException {
		externalScript
				.launch("ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -i scripts/avanza.pem root@"
						+ externalNode.getIpAddress() + " " + scriptPath + " -queue " + queueName);
	}
	@Override
	public void executeSlurmd (ExternalScript externalScript, ExternalNode externalNode, String scriptPath) {
		externalScript
		.launch("ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -i scripts/avanza.pem root@"
				+ externalNode.getIpAddress() + " " + scriptPath);
	}

	@Override
	public boolean deleteExperimentCloudInformation(ExternalScript externalScriptLauncher, Experiment experiment) {

		HttpTransport httpTransport;
		boolean result = true;

		List<String> instancesName = new ArrayList<String>();

		try {

			httpTransport = GoogleNetHttpTransport.newTrustedTransport();

			// Authenticate using Google Application Default Credentials.
			GoogleCredential credential = getCredentials();

			// Create Compute Engine object for listing instances.
			Compute compute = new Compute.Builder(httpTransport, JSON_FACTORY, credential)
					.setApplicationName(APPLICATION_NAME).build();

			List<ExternalNode> experimentNodes = ExternalNode.findByExperiment(experiment);

			for (ExternalNode cloudNode : experimentNodes) {
				cloudNode.delete();
				instancesName.add(cloudNode.getNameInSlurm());
				deleteInstance(compute, cloudNode.getExternalId());
			}
		} catch (Exception e) {
			result = false;
		}
		removeQueueFromSlurm(externalScriptLauncher, experiment, instancesName);

		if (!result) {
			experiment.addWarningEvent(String.format(
					"Warning when finishing experiment %s. Can not terminate cloud instances. Support information succesfully removed. Cloud instances will terminate themselves",
					experiment.name));
		}

		return false;
	}

	private static Operation deleteInstance(Compute compute, String instanceName) throws Exception {
		System.out.println("================== Deleting Instance " + instanceName + " ==================");
		Compute.Instances.Delete delete = compute.instances().delete(PROJECT_ID, ZONE_NAME, instanceName);
		return delete.execute();
	}

	private static GoogleCredential getCredentials() throws IOException {
		GoogleCredential credential = GoogleCredential.getApplicationDefault();
		if (credential.createScopedRequired()) {
			List<String> scopes = new ArrayList<>();
			// Set Google Cloud Storage scope to Full Control.
			scopes.add(ComputeScopes.DEVSTORAGE_FULL_CONTROL);
			// Set Google Compute Engine scope to Read-write.
			scopes.add(ComputeScopes.COMPUTE);
			credential = credential.createScoped(scopes);
		}
		return credential;
	}

	private ExternalNode createWorker(Compute compute, String instanceName, Experiment experiment)
			throws IOException, InterruptedException {

		System.out.println("================== Create disk ==================");
		String requestBody = "{'name': '" + instanceName + "','sourceSnapshot': 'projects/" + PROJECT_ID
				+ "/global/snapshots/snapshot-worker','sizeGb': '8','type': 'projects/" + PROJECT_ID + "/zones/"
				+ ZONE_NAME + "/diskTypes/pd-standard','zone': 'projects/" + PROJECT_ID + "/zones/" + ZONE_NAME + "'}";

		HttpRequest request = compute.getRequestFactory()
				.buildPostRequest(new GenericUrl("https://www.googleapis.com/compute/v1/projects/" + PROJECT_ID
						+ "/zones/" + ZONE_NAME + "/disks"),
						ByteArrayContent.fromString("application/json", requestBody));
		request.getHeaders().setContentType("application/json");

		HttpResponse response = request.execute();

		while (!isDiskReady(compute, instanceName)) {
			Thread.sleep(3000);
		}

		requestBody = "{'name': '" + instanceName + "','zone': 'projects/" + PROJECT_ID + "/zones/" + ZONE_NAME
				+ "','machineType': 'projects/" + PROJECT_ID + "/zones/" + ZONE_NAME + "/machineTypes/" + INSTANCE_TYPE
				+ "',  'metadata': {    'items': []  },  'tags': {    'items': []  },  'disks': [    {      'type': 'PERSISTENT',      'boot': true,      'mode': 'READ_WRITE',      'autoDelete': true,      'deviceName': '"
				+ instanceName + "',      'source': 'projects/" + PROJECT_ID + "/zones/" + ZONE_NAME + "/disks/"
				+ instanceName
				+ "'    }  ],  'canIpForward': false,  'networkInterfaces': [    {      'network': 'projects/"
				+ PROJECT_ID + "/global/networks/avanza',      'subnetwork': 'projects/" + PROJECT_ID
				+ "/regions/us-central1/subnetworks/avanza',      'accessConfigs': [        {          'name': 'External NAT',          'type': 'ONE_TO_ONE_NAT'        }      ],      'aliasIpRanges': []    }  ],  'description': '',  'labels': {},  'scheduling': {    'preemptible': false,    'onHostMaintenance': 'MIGRATE',    'automaticRestart': true  },  'serviceAccounts': [    {      'email': '527187498606-compute@developer.gserviceaccount.com',      'scopes': [        'https://www.googleapis.com/auth/devstorage.read_only',        'https://www.googleapis.com/auth/logging.write',        'https://www.googleapis.com/auth/monitoring.write',        'https://www.googleapis.com/auth/servicecontrol',        'https://www.googleapis.com/auth/service.management.readonly',        'https://www.googleapis.com/auth/trace.append'      ]    }  ]}";

		request = compute.getRequestFactory().buildPostRequest(new GenericUrl(
				"https://www.googleapis.com/compute/v1/projects/" + PROJECT_ID + "/zones/" + ZONE_NAME + "/instances"),
				ByteArrayContent.fromString("application/json", requestBody));
		request.getHeaders().setContentType("application/json");

		response = request.execute();

		ExternalNode externalNode = null;

		while (externalNode == null) {
			Thread.sleep(3000);
			externalNode = isInstanceReady(compute, instanceName, experiment);
		}
		return externalNode;

	}

	private boolean isDiskReady(Compute compute, String instanceName) throws IOException {
		System.out.println("================== Checking disk ready ==================");
		Compute.Disks.List disks = compute.disks().list(PROJECT_ID, ZONE_NAME);
		DiskList list = disks.execute();
		boolean found = false;
		if (list.getItems() == null) {
			System.out.println("No instances found. Sign in to the Google Developers Console and create "
					+ "an instance at: https://console.developers.google.com/");
		} else {
			for (Disk disk : list.getItems()) {
				System.out.println(disk.toPrettyString());
				if (disk.getName().equals(instanceName)) {
					if (disk.getStatus().equalsIgnoreCase("READY")) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
		return found;
	}

	private ExternalNode isInstanceReady(Compute compute, String instanceName, Experiment experiment)
			throws IOException {
		System.out.println("================== Checking instance ready ==================");
		Compute.Instances.List instances = compute.instances().list(PROJECT_ID, ZONE_NAME);
		InstanceList list = instances.execute();
		if (list.getItems() == null) {
			System.out.println("No instances found. Sign in to the Google Developers Console and create "
					+ "an instance at: https://console.developers.google.com/");
		} else {
			for (Instance instance : list.getItems()) {
				System.out.println(instance.toPrettyString());
				if (instance.getName().equals(instanceName)) {
					if (instance.getStatus().equalsIgnoreCase("RUNNING")) {
						return createNewNode(instance, experiment);
					}
				}
			}
		}
		return null;
	}

	private ExternalNode createNewNode(Instance instance, Experiment experiment) throws IOException {
		ExternalNode externalNode = new ExternalNode();
		externalNode.setExperiment(experiment);
		externalNode.setExternalId(instance.getName());
		externalNode.setExternalProvider(provider);

		List<NetworkInterface> networkInterfaces = instance.getNetworkInterfaces();
		for (NetworkInterface networkInterface : networkInterfaces) {
			System.out.println(networkInterface.toPrettyString());
			List<AccessConfig> accessConfigs = networkInterface.getAccessConfigs();
			for (AccessConfig accessConfig : accessConfigs) {
				System.out.println(accessConfig.toPrettyString());
				if (accessConfig.getName().equalsIgnoreCase("External NAT")) {
					externalNode.setIpAddress(accessConfig.getNatIP());
					externalNode.setPrivateIpAddress(networkInterface.getNetworkIP());
				}
			}
		}
		externalNode.setStatus(instance.getStatus());
		externalNode.setPrivateDns(instance.getName() + ".c." + PROJECT_ID + ".internal");

		externalNode.save();
		logger.info("External node saved " + externalNode.id);
		return externalNode;
	}

	private void copyToRemote(ExternalScript externalScript, ExternalNode externalNode, String origin,
			String destination) throws InterruptedException {
		int retries = 100;
		while (retries > 0) {
			try {
				retries--;
				externalScript.launch(
						"scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -i scripts/avanza.pem -p "
								+ origin + " root@" + externalNode.getIpAddress() + ":" + destination);
				break;
			} catch (Exception e) {
				System.out.println("Connection refused, trying again...");
				Thread.sleep(3000);
			}
		}
	}

	protected String getNewInstanceName() {
		return APPLICATION_NAME + "-" + (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")).format(new Date());
	}

	@Override
	public boolean isAmazon() {
		return false;
	}

	@Override
	public boolean isGoogle() {
		return true;
	}

	@Override
	public boolean isAzure() {
		return false;
	}

}
