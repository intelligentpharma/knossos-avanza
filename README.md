# knossos-avanza project
Avanza part for the knossos project extending the actual private computing capabilities to cloud computing and mixed computing (private + cloud).

Able to operate with any combination of Azure Cloud, Amazon Computing and Google Cloud
# Contents

## App
	· ExperimentManager: job balancer functionalities for the experiment
	· WorkflowManager: managed workflows
	· AbstractBidimensionalEngine: manages cloud experiment tasks
	· ExternalNodeCreatorJob: Add external computing node information from DB
	· ExternalNodeRemoverJob: Remove external computing node information from DB
	· MedeaPredictionExperimentLauncher: launch medea experiments, including cloud management tasks
	· PythiaExperimentLauncher: launch experiments, including cloud management tasks
	· ValueComparator: Compare values of cloud experiments
	· ExternalNode: External computing node
	· Workflow: Workflows
	· WorkflowExperiment: Experiments inside workflows
	· AWSNodesManager: Management of Amazon WS nodes
	· AzureNodesManager: Management of Azure nodes
	· ExternalNodesManager: DB management of cloud nodes
	· ExternalNodesManagerException: Exceptions for DB management of cloud nodes
	· GcloudNodesManager: Creation of Google Cloud VM
	· ComplexityEstimationCore: Complexity estimations for jobs, usefull to balance them on hibrid configurations
	· SBRABalance: Wrapper for hibrid job balancer
	· SBRAConstants: Constants for hibrid job balancer	
	· SBRACore: Core for hibrid job balancer
	· SBRAException: Exceptions for hibrid job balancer
	· AzureVMCreator: Creation of Azure VM
	· Factory: Factory for creating workflows and workflow databases
	· FactoryImpl: Factory implementation for creating workflows and workflow databases
	· getAllExperimentsForWorkflow: View to list all experiments of a particular workflow
	· addStep: View to add step to worklow
	· addWorkflow: View to add new workflow	
	· getWorkflow: View to retrieve information from workflow
	· listWorkflowsOwnedBy: View list all workflows owned by particular user
## Scripts
	· agregar_worker_aws: Add cloud node as worker to slurm
	· autoshutdown: Automatically stop cloud nodes if they are iddle for a while	
	· borrar_cola_hidra: Slurm management, removing cloud instance queue
	· check_connection: Check if ssh connection is open on cloud node
	· conectar_vpn: Connecting VPN to cloud instances
	· configurar_hidra: Reconfigure slurm queues
	· configurar_slurm: Add generic cloud node as worker to slurm
	· crear_cola_hidra: Slurm management, adding cloud instance queue
	· desconectar_vpn: Disconnecting VPN to cloud instances
	· sacar_worker_aws: Remove cloud node as worker from slurm
	· start_slurm_services: Configure cloud node as worker to slurm
	· userData_gcloud: Sending user data information to google cloud instance
	· azure_addnode: Add Azure cloud node to slurm configuration
	· azure_delpartition: Remove slurm queue with cloud nodes on it
	· azure_addpartition: Add slurm queue with cloud nodes on it
	· azure_delnode: Remove Azure cloud node from slurm configuration
	· slurm_install_azure_v2: Install slurm software on cloud nodes. Usefull when generating new cloud images
	· nfs_azure: Install nfs software on cloud nodes. Usefull when generating new cloud images for sharing files over disks
	· openvpn_azure_client: Install open vpn software on cloud nodes. Usefull when generating new cloud images for securing connections between internal and cloud computing nodes
	· sendkeys: Sends private keys to azure computing nodes. Usefull when generating new cloud images
	· disconnect_azureVPN: Unstablish secure connection between cloud and internal computing nodes
	· openvpn_azure_server: Stablish secure connection between cloud and internal computing nodes
	· connect_azureVPN: Starts VPN cloud manager and adds internal computing resources to the private network
## Test
	· SBRACoreAuxFunctionsTest: Hibrid job balancer	test
	· SBRACoreBalanceTest: Hibrid job balancer	test
	· SBRACoreGetEstimationTest: Hibrid job balancer test

# List or required libraries
· See lib directory

# License
Copyright (c) 2017 Intelligent Pharma SLA

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies,
either expressed or implied, of the FreeBSD Project.

## JSCH
This software uses JSch

JSch 0.0.* was released under the GNU LGPL license.  Later, we have switched 
over to a BSD-style license. 

------------------------------------------------------------------------------
Copyright (c) 2002-2015 Atsuhiko Yamanaka, JCraft,Inc. 
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright 
     notice, this list of conditions and the following disclaimer in 
     the documentation and/or other materials provided with the distribution.

  3. The names of the authors may not be used to endorse or promote products
     derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
