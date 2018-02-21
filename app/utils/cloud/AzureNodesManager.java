package utils.cloud;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.LogLevel;

import models.Experiment;
import models.ExternalNode;
import utils.TemplatedConfiguration;
import utils.scripts.ExternalScript;

public class AzureNodesManager extends ExternalNodesManager {
	private static final String RESOURCE_GROUP = "AzureTestResourceGroup";

	public static final Logger logger = Logger.getLogger(AzureNodesManager.class);

	public static final String PROVIDER_NAME = "Azure";

	private static final int WAIT_TIME_TO_CHECK_NODES = 120000; // Two minutes

	private static final String user = "azureAdmin";
	private static final String password = "19TsomI90";

	public AzureNodesManager() {
		provider = PROVIDER_NAME;
		initializationTime = WAIT_TIME_TO_CHECK_NODES;
	}

	@Override
	public Set<ExternalNode> createOnDemandInstances(int size, Experiment experiment, String queueName,
			ExternalScript externalScript) throws ExternalNodesManagerException {

		Set<ExternalNode> runningInstances = new HashSet<ExternalNode>();

		for (int i = 0; i < size; i++) {
			runningInstances.add(createVMImage(getNewInstanceName(), experiment, externalScript));
		}
		return runningInstances;
	}

	@Override
	public boolean deleteExperimentCloudInformation(ExternalScript externalScriptLauncher, Experiment experiment) {
		Azure azure = initAzure();
		logger.info(String.format("Removing experiment cloud information with experiment id '%d'", experiment.getId()));
		if (experiment.numberOfCloudInstances <= 0) {
			return true;
		}
		List<ExternalNode> experimentNodes = ExternalNode.findByExperiment(experiment);		
		for (ExternalNode cloudNode : experimentNodes) {

			VirtualMachine virtualMachine = azure.virtualMachines().getByResourceGroup(RESOURCE_GROUP,
					cloudNode.getExternalId());
			
			cloudNode.delete();
			
			removeQueueFromSlurm(externalScriptLauncher, experiment);
			
			if (virtualMachine != null) {				
				String networkInterfaceId = virtualMachine.getPrimaryNetworkInterface().id();
				String publicIpId = virtualMachine.getPrimaryPublicIPAddressId();
				String diskId = virtualMachine.osDiskId();
				String virtualMachineId = virtualMachine.id();
				
				logger.info("Stopping a Linux VM in Azure, be patient it may take a while");
				virtualMachine.deallocate();
				logger.info("	Deleting virtual machine");
				azure.virtualMachines().deleteById(virtualMachineId);
				logger.info("	Deleting disks");
				azure.disks().deleteById(diskId);
				logger.info("	Deleting network interface");
				azure.networkInterfaces().deleteById(networkInterfaceId);
				logger.info("	Deleting ip adress");
				azure.publicIPAddresses().deleteById(publicIpId);
				logger.info("Stopped a Linux VM in Azure");
			}

			

//			if (!result) {
//				experiment.addWarningEvent(String.format(
//						"Warning when finishing experiment %s. Can not terminate cloud AWS instances. Support information succesfully removed. AWS instances will terminate themselves",
//						experiment.name));
//			}

		}
		return true;
	}

	private String removeQueueFromSlurm(ExternalScript externalScriptLauncher, Experiment experiment) {
		String command = String.format(TemplatedConfiguration.get("delete_partition_azure"), experiment.id);
		return externalScriptLauncher.launch(command);	
	}

	@Override
	protected String getNewInstanceName() {
		return APPLICATION_NAME + "-" + (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")).format(new Date());
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
		logger.info("Starting slurm on node " + externalNode.getName());
		startSlurm(externalScript, externalNode);
	}

	public ExternalNode createVMImage(String instanceName, Experiment experiment,
			ExternalScript externalScriptLauncher) {
		logger.info("Creating a Linux VM in Azure from custom image");

		ExternalNode externalNode = new ExternalNode();

		try {
			Azure azure = initAzure();

			AvailabilitySet availabilitySet = azure.availabilitySets().getById(
					"/subscriptions/cc0230ea-e1a7-4ea3-900f-13f1e5b5c53c/resourceGroups/AzureTestResourceGroup/providers/Microsoft.Compute/availabilitySets/AzureTestAvailabilitySet");

			logger.info("	Creating public IP address...");
			PublicIPAddress publicIPAddress = azure.publicIPAddresses().define("AzureTestPublicIP" + instanceName)
					.withRegion(Region.UK_WEST).withExistingResourceGroup(RESOURCE_GROUP).withDynamicIP().create();

			logger.info("	Creating virtual network...");
			azure.networks().define("");
			Network network = azure.networks().getById(
					"/subscriptions/cc0230ea-e1a7-4ea3-900f-13f1e5b5c53c/resourceGroups/AzureTestResourceGroup/providers/Microsoft.Network/virtualNetworks/AvanzaNet");

			logger.info("	Creating network interface...");
			NetworkInterface networkInterface = azure.networkInterfaces().define("AzureTestNIC" + instanceName)
					.withRegion(Region.UK_WEST).withExistingResourceGroup(RESOURCE_GROUP)
					.withExistingPrimaryNetwork(network).withSubnet("avanzaSubnet").withPrimaryPrivateIPAddressDynamic()
					.withExistingPrimaryPublicIPAddress(publicIPAddress).create();

			logger.info(" Creating virtual machine...");
			VirtualMachine virtualMachine = azure.virtualMachines().define("AzureTestVM" + instanceName)
					.withRegion(Region.UK_WEST).withExistingResourceGroup(RESOURCE_GROUP)
					.withExistingPrimaryNetworkInterface(networkInterface)
					.withLinuxCustomImage(
							"/subscriptions/cc0230ea-e1a7-4ea3-900f-13f1e5b5c53c/resourceGroups/AZURETESTRESOURCEGROUP/providers/Microsoft.Compute/images/AzureVMImageSlurm")
					.withRootUsername(this.user).withRootPassword(this.password)
					.withComputerName("AzureTestVM" + instanceName).withExistingAvailabilitySet(availabilitySet)
					.withSize("Standard_F4s").create();
			logger.info("Created a Linux VM in Azure");

			PublicIPAddress publicIp = azure.publicIPAddresses().getById(virtualMachine.getPrimaryPublicIPAddressId());
			String publicIP = publicIp.ipAddress();
			String nodeName = "AzureTestVM" + instanceName;
			// externalNode.setName(instanceName);
			externalNode.setExperiment(experiment);
			externalNode.setExternalId(nodeName);
			externalNode.setExternalProvider(provider);
			externalNode.setIpAddress(publicIp.ipAddress());
			externalNode.setPrivateIpAddress(getPrivateIP(publicIp.ipAddress(), externalScriptLauncher));
			externalNode.setPrivateDns("TBD");
			externalNode.save();
			logger.info("Node information");
			logger.info("	· id: " + "AzureTestVM" + instanceName);
			logger.info("	· ipAddress: " + publicIp.ipAddress());
			logger.info("	· privateIpAddress: " + externalNode.getPrivateIpAddress());
			logger.info("	· name: " + virtualMachine.name());
			logger.info("	· externalId" + virtualMachine.id());
			logger.info("	· externalProvider" + "Azure");
			logger.info("	· status" + "Available");
			logger.info("	· privateDns" + "TBD");

		} catch (Exception e) {
			logger.info(e.getMessage());
			e.printStackTrace();
		}

		logger.info("Created a Linux VM in Azure");
		return externalNode;
	}

	private Azure initAzure() {
		Azure azure = null;
		try {
			logger.info("	Creating credentials");
			// Load from configuration file?
			String client = "2435f7fc-0faf-4140-93ca-fcc13659780e";
			String tenant = "10bff8d7-f7a4-410c-82e1-05c3a8f0cfb3";
			String key = this.password;
			ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(client, tenant, key,
					AzureEnvironment.AZURE);

			azure = Azure.configure().withLogLevel(LogLevel.BASIC).authenticate(credentials)
					.withDefaultSubscription();
		} catch (IOException e) {
			throw new RuntimeException("Error getting credentials from Azure " + e.getMessage());
		}
		return azure;
	}

	@Override
	public void createSlurmPartition(ExternalScript externalScriptLauncher, String queueName, Set<ExternalNode> nodes) {
		for (ExternalNode node : nodes) {
			addNodeToSlurm(externalScriptLauncher, node, queueName);
		}
		logger.info("Reconfiguring slurm");
		reconfigureSlurm(externalScriptLauncher);

	}

	private String startSlurm(ExternalScript externalScriptLauncher, ExternalNode node) {
		String command = String.format(TemplatedConfiguration.get("connect_to_azure_node"), node.getIpAddress(),
				"/root/azure/slurmd_start.sh");
		return externalScriptLauncher.launch(command);
	}

	private String getPrivateIP(String publicIp, ExternalScript externalScriptLauncher) {
		String command = String.format(TemplatedConfiguration.get("connect_to_azure_node"), publicIp, "privateip");
		String IP = externalScriptLauncher.launch(command);
		String privateIP = IP.trim().replace("\n", "");
		return privateIP;
	}

	private String addNodeToSlurm(ExternalScript externalScriptLauncher, ExternalNode node, String queueName) {
		String command = String.format(TemplatedConfiguration.get("create_partition_azure"), node.getExternalId(),
				node.getPrivateIpAddress(), queueName);
		return externalScriptLauncher.launch(command);
	}

	// Runs a script on hidra to reconfig slurm
	private String reconfigureSlurm(ExternalScript externalScriptLauncher) {
		String command = String.format(TemplatedConfiguration.get("reconfigure_slurm"));
		return externalScriptLauncher.launch(command);
	}

	@Override
	public boolean areNodesAvailable(ExternalScript externalScriptLauncher, Set<ExternalNode> nodes,
			String cloudQueueName) {
		return true;
	}

}
