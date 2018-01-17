package utils;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.AvailabilitySetSkuTypes;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.InstanceViewStatus;
import com.microsoft.azure.management.compute.DiskInstanceView;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.PublicIPAddresses;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.rest.LogLevel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Random;
import java.util.Scanner;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class AzureVMCreator {
	
	Azure azure;
	ResourceGroup resourceGroup;
	AvailabilitySet availabilitySet;
	PublicIPAddress publicIPAddress;
	Network network;
	NetworkInterface networkInterface;
	VirtualMachine virtualMachine;
	String publicIP = "";
	String user = "azureAdmin";
	String password = "19TsomI90";
	int randomLimit = 20000;
	String partition="void";
	String privateIP;
	String nodeName;
	
	public String getPartition(){
		return this.partition;
	}
	
	public void setPartition(String partition){
		this.partition = partition;
	}
	
	//Generates a random part for the name
		private String randomName(){		
			Random rand = new Random();

			int  n = rand.nextInt(randomLimit) + 1;
			return Integer.toString(n);
		}
	
	//Fill manually fields for testing
	public void fillData(String publicIP, String privateIP, String nodeName, String partition){
		this.publicIP = publicIP;
		this.privateIP = privateIP;
		this.nodeName = nodeName;
		this.partition = partition;
	}	
	
	//Runs a script via ssh
	public String runScript(String identity, String host, String command){		
		System.out.println("Running command : '" + command + "' on host '" + host + "' with identity '" + command + "'");
	    int port=22;
	    String result = "";
	    try        {	
	    	
	    	JSch jsch = new JSch();
	    	/*
	    	 * This requires private key on your dev machine
	    	 * to copy it run from hidra-01 as root: 
	    	 * 	scp /root/.ssh/id_rsa igomez@hermes:/home/ip-users/igomez/.ssh/id_rsa_azure
	    	 */
	    	jsch.addIdentity(identity);
	    	Session session = jsch.getSession("root", host, port);	    	    	
            session.setConfig("StrictHostKeyChecking", "no");
            System.out.println("Connecting to " + host +  "...");
            session.connect();
            System.out.println("Connection established to " + host);                       
            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            InputStream in=channel.getInputStream();
            channel.connect();

            byte[] tmp=new byte[1024];
            while(true){
              while(in.available()>0){
                int i=in.read(tmp, 0, 1024);
                if(i<0)break;
                result = new String(tmp, 0, i);                
              }
              if(channel.isClosed()){
                if(in.available()>0) continue;                 
                break;
              }
              try{Thread.sleep(1000);}catch(Exception ee){}
            }                        
            
            channel.disconnect();
            session.disconnect();
        }
	    catch(Exception e){
	    	System.err.print(e);
    	}
	    
	    return result;
	}
	
	//Runs a script on hidra to add a partition to slurm
		public String addPartitionToSlurm(){
			String host = "hidra-01";
			String identity = "/home/ip-users/igomez/.ssh/id_rsa";
			if (this.partition.equalsIgnoreCase("void")){
        		this.partition = "AZURE_" + randomName();
        	}
			String command= "/root/avanza/azure/azure_addpartition.sh " + this.partition;         			
			return runScript(identity, host, command);		    
		}
		
		//Runs a script on hidra to remove a partition from slurm
		public String removePartitionFromSlurm(){
			
			String host = "hidra-01";
			String identity = "/home/ip-users/igomez/.ssh/id_rsa";
			if (this.partition.equalsIgnoreCase("void")){
        		this.partition = "AZURE_" + randomName();
        	}
			String command= "/root/avanza/azure/azure_delpartition.sh " + this.partition;         			
			return runScript(identity, host, command);		    			
		    
		}
	
		//Runs a script on hidra to node to slurm
		public String addNodeToSlurm(){
			String host = "hidra-01";
			String identity = "/home/ip-users/igomez/.ssh/id_rsa";
			if (this.partition.equalsIgnoreCase("void")){
        		this.partition = "AZURE_" + randomName();
        	}
			String command= "./avanza/azure/azure_addnode.sh " + this.nodeName + " " + this.privateIP + " " + this.partition;   		
			return runScript(identity, host, command);		
		}	
		
		//Runs a script on hidra to reconfig slurm
		public String reconfigSlurm(){
			String host = "hidra-01";
			String identity = "/home/ip-users/igomez/.ssh/id_rsa";			
			String command= "systemctl restart slurm";   		
			return runScript(identity, host, command);
		}
		
		//Runs a script on the VM to start slurm
		public String startSlurm(){
			String host = publicIP;
			String identity = "/home/ip-users/igomez/.ssh/id_rsa_azure";			
			String command= "/root/azure/slurmd_start.sh";   		
			return runScript(identity, host, command);
		}	
		
	//Runs a script on the VM to obtain private IP adress
	public String getPrivateIP(){
		String host = publicIP;
		String identity = "/home/ip-users/igomez/.ssh/id_rsa_azure";			
		String command= "privateip";   		
		String IP = runScript(identity, host, command);
		this.privateIP = IP.trim().replace("\n", "");
		return IP;
	}
	
	//Starts the virtual machine
	public void startVM(){
		System.out.println("Starting vm...");
		this.virtualMachine.start();		
	}
	
	//Stops the virtual machine and deletes resources associated
	public void stopVM(){
		System.out.println("Stopping a Linux VM in Azure, be patient it may take a while");		
		virtualMachine.deallocate();		
		System.out.println("	Deleting virtual machine");
		String diskId = virtualMachine.osDiskId();
		azure.virtualMachines().deleteById(virtualMachine.id());
		System.out.println("	Deleting disks");
		azure.disks().deleteById(diskId);
		System.out.println("	Deleting network interface");
		azure.networkInterfaces().deleteById(networkInterface.id());		
		//System.out.println("	Deleting network");
		//azure.networks().deleteById(network.id());		
		System.out.println("	Deleting ip adress");
		azure.publicIPAddresses().deleteById(publicIPAddress.id());
		//System.out.println("	Deleting AvailabilitySet");
		//azure.availabilitySets().deleteById(availabilitySet.id());		
		//System.out.println("	Deleting resourceGroup");
		//azure.resourceGroups().deleteByName(resourceGroup.name());		
		
		System.out.println("Stopped a Linux VM in Azure");
	}
	
	//Creates Azure VM with default debian distribution
	public void createVM(){
		System.out.println("Creating a Linux VM in Azure from premade image");
		
		try {    
			System.out.println("	Creating credentials");
			//Load from configuration file?
			String client = "2435f7fc-0faf-4140-93ca-fcc13659780e";
			String tenant = "10bff8d7-f7a4-410c-82e1-05c3a8f0cfb3";
			String key = this.password;
			ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
					client, tenant, key, AzureEnvironment.AZURE);
			
		    azure = Azure.configure()
		        .withLogLevel(LogLevel.BASIC)
		        .authenticate(credentials)
		        .withDefaultSubscription();			   			    
		    
		    availabilitySet = azure.availabilitySets().getById("/subscriptions/cc0230ea-e1a7-4ea3-900f-13f1e5b5c53c/resourceGroups/AzureTestResourceGroup/providers/Microsoft.Compute/availabilitySets/AzureTestAvailabilitySet");			    			   
		    String random = randomName();
		    //random = "";
		    System.out.println("	Creating public IP address...");
		    publicIPAddress = azure.publicIPAddresses()
		    	    .define("AzureTestPublicIP" + random)
		    	    .withRegion(Region.UK_WEST)
		    	    .withExistingResourceGroup("AzureTestResourceGroup")
		    	    .withDynamicIP()
		    	    .create();
		    
		    
		    System.out.println("	Creating virtual network...");		
		    azure.networks().define("");
		    network = azure.networks().getById("/subscriptions/cc0230ea-e1a7-4ea3-900f-13f1e5b5c53c/resourceGroups/AzureTestResourceGroup/providers/Microsoft.Network/virtualNetworks/AvanzaNet");
		    
		    System.out.println("	Creating network interface...");
		    networkInterface = azure.networkInterfaces()
		        .define("AzureTestNIC" + random)
		        .withRegion(Region.UK_WEST)
		        .withExistingResourceGroup("AzureTestResourceGroup")
		        .withExistingPrimaryNetwork(network)
		        .withSubnet("avanzaSubnet")			        
		        .withPrimaryPrivateIPAddressDynamic()
		        .withExistingPrimaryPublicIPAddress(publicIPAddress)
		        .create();			   			    
		    
		    System.out.println(" Creating virtual machine...");
		    virtualMachine = azure.virtualMachines()
		        .define("AzureTestVM" + random)
		        .withRegion(Region.UK_WEST)
		        .withExistingResourceGroup("AzureTestResourceGroup")
		        .withExistingPrimaryNetworkInterface(networkInterface)
		        .withLatestLinuxImage("credativ", "Debian", "9")
		        .withRootUsername(this.user)
		        .withRootPassword(this.password)
		        .withComputerName("AzureTestVM" + random)
		        .withExistingAvailabilitySet(availabilitySet)
		        .withSize("Standard_F4s")
		        .create();
		    System.out.println("Created a Linux VM in Azure");		
		    
		    PublicIPAddress publicIp = azure.publicIPAddresses().getById(virtualMachine.getPrimaryPublicIPAddressId());
		    this.publicIP = publicIp.ipAddress();
		    this.nodeName = "AzureTestVM" + random;
		    System.out.println("Node information");
		    System.out.println("	· id: " + " Auto");
		    System.out.println("	· ipAddress: " + publicIp.ipAddress());
		    System.out.println("	· privateIpAddress: " + networkInterface.primaryPrivateIP());
		    System.out.println("	· name: " + virtualMachine.name());
		    System.out.println("	· externalId" + virtualMachine.id());
		    System.out.println("	· externalProvider" + "Azure");
		    System.out.println("	· status" + "Available");
		    System.out.println("	· privateDns" + "TBD");
		    
		} catch (Exception e) {
		    System.out.println(e.getMessage());
		    e.printStackTrace();
		}
		
		
		
    	System.out.println("Created a Linux VM in Azure");
	}
	
	//Creates Azure VM with custom image
		public void createVMImage(){
			System.out.println("Creating a Linux VM in Azure from custom image");
			
			try {    
				System.out.println("	Creating credentials");
				//Load from configuration file?
				String client = "2435f7fc-0faf-4140-93ca-fcc13659780e";
				String tenant = "10bff8d7-f7a4-410c-82e1-05c3a8f0cfb3";
				String key = this.password;
				ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
						client, tenant, key, AzureEnvironment.AZURE);
				
			    azure = Azure.configure()
			        .withLogLevel(LogLevel.BASIC)
			        .authenticate(credentials)
			        .withDefaultSubscription();			   			    
			    
			    availabilitySet = azure.availabilitySets().getById("/subscriptions/cc0230ea-e1a7-4ea3-900f-13f1e5b5c53c/resourceGroups/AzureTestResourceGroup/providers/Microsoft.Compute/availabilitySets/AzureTestAvailabilitySet");			    			   
			    String random = randomName();
			    System.out.println("	Creating public IP address...");
			    publicIPAddress = azure.publicIPAddresses()
			    	    .define("AzureTestPublicIP" + random)
			    	    .withRegion(Region.UK_WEST)
			    	    .withExistingResourceGroup("AzureTestResourceGroup")
			    	    .withDynamicIP()
			    	    .create();
			    
			    
			    System.out.println("	Creating virtual network...");		
			    azure.networks().define("");
			    network = azure.networks().getById("/subscriptions/cc0230ea-e1a7-4ea3-900f-13f1e5b5c53c/resourceGroups/AzureTestResourceGroup/providers/Microsoft.Network/virtualNetworks/AvanzaNet");
			    
			    System.out.println("	Creating network interface...");
			    networkInterface = azure.networkInterfaces()
			        .define("AzureTestNIC" + random)
			        .withRegion(Region.UK_WEST)
			        .withExistingResourceGroup("AzureTestResourceGroup")
			        .withExistingPrimaryNetwork(network)
			        .withSubnet("avanzaSubnet")			        
			        .withPrimaryPrivateIPAddressDynamic()
			        .withExistingPrimaryPublicIPAddress(publicIPAddress)
			        .create();			   			    
			    
			    System.out.println(" Creating virtual machine...");
			    virtualMachine = azure.virtualMachines()
			        .define("AzureTestVM"+ random)
			        .withRegion(Region.UK_WEST)
			        .withExistingResourceGroup("AzureTestResourceGroup")
			        .withExistingPrimaryNetworkInterface(networkInterface)
			        .withLinuxCustomImage("/subscriptions/cc0230ea-e1a7-4ea3-900f-13f1e5b5c53c/resourceGroups/AZURETESTRESOURCEGROUP/providers/Microsoft.Compute/images/AzureVMImageSlurm")
			        .withRootUsername(this.user)
			        .withRootPassword(this.password)
			        .withComputerName("AzureTestVM"+ random)
			        .withExistingAvailabilitySet(availabilitySet)
			        .withSize("Standard_F4s")
			        .create();
			    System.out.println("Created a Linux VM in Azure");		
			    
			    PublicIPAddress publicIp = azure.publicIPAddresses().getById(virtualMachine.getPrimaryPublicIPAddressId());
			    this.publicIP = publicIp.ipAddress();			    
			    this.nodeName = "AzureTestVM" + random;
			    System.out.println("Node information");
			    System.out.println("	· id: " + "AzureTestVM" + random);
			    System.out.println("	· ipAddress: " + publicIp.ipAddress());
			    System.out.println("	· privateIpAddress: " + networkInterface.primaryPrivateIP());
			    System.out.println("	· name: " + virtualMachine.name());
			    System.out.println("	· externalId" + virtualMachine.id());
			    System.out.println("	· externalProvider" + "Azure");
			    System.out.println("	· status" + "Available");
			    System.out.println("	· privateDns" + "TBD");
			    
			} catch (Exception e) {
			    System.out.println(e.getMessage());
			    e.printStackTrace();
			}
			
			
			
	    	System.out.println("Created a Linux VM in Azure");
		}


}
