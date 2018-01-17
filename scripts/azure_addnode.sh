#!/bin/bash
# Author:		ggutierrez @ Intelligent Pharma - 2017
# Description:	helper functions for azure's slurm partition and nodes management


# az_addnode <nodename> <nodeaddress> <partition>  [<config file=/grid/env/slurm/slurm.conf>]
#	adds a node with nodename and ip address to a partition
if [ ${#@} -lt 3 ]; then
	echo not enough arguments >&2
	echo "Usage:"
	echo '	addnode <nodename> <nodeaddress> <partition>  [<config file=/grid/env/slurm/slurm.conf>]'
	return
fi
AZ_NODENAME=$1
AZ_NODEADDR=$2
AZ_PARTNAME=$3
export conffile=${4:-'/grid/env/slurm/slurm.conf'}
touch $conffile

# get the values for node specs
if [ -f $AVANZAHOME/azure/azure.cfg ]; then
	source $AVANZAHOME/azure/azure.cfg
else
	echo "$AVANZAHOME/azure/azure.cfg not found (is \$AVANZAHOME set?)" >&2
	echo "using default values..." >&2
	#these are default values. if something changes they may be wrong
	export AZ_NODE_REALMEMORY=6144
	export AZ_NODE_SOCKETS=2
	export AZ_NODE_CORESPERSOCKET=2
	export AZ_NODE_THREADSPERCORE=1
fi

# build the Node description line
AZ_NODELINE=""
AZ_NODELINE="NodeName=${AZ_NODENAME}"
AZ_NODELINE="$AZ_NODELINE NodeAddr=${AZ_NODEADDR}"
AZ_NODELINE="$AZ_NODELINE Sockets=${AZ_NODE_SOCKETS}"
AZ_NODELINE="$AZ_NODELINE CoresPerSocket=${AZ_NODE_CORESPERSOCKET}"
AZ_NODELINE="$AZ_NODELINE ThreadsPerCore=${AZ_NODE_THREADSPERCORE}"
AZ_NODELINE="$AZ_NODELINE RealMemory=${AZ_NODE_REALMEMORY}"
AZ_NODELINE="$AZ_NODELINE State=IDLE"
if [ "$(cat $conffile | grep '^#<partition name=\"'${AZ_PARTNAME}'\">' )" != "" ]; then
	echo "partition found, adding node" >&2
	NodePresent="$(cat cosa.cfg | sed -n -e '/^#<partition name=\"'${AZ_PARTNAME}'\">/,/#<\/partition>/p' | grep '^NodeName='${AZ_NODENAME}' ')"
	if [ "${NodePresent}" == "" ]; then
		sed -i -e '/^#<partition name="'${AZ_PARTNAME}'">/,/#<\/partition>/ {
			s/\(^#<partition name="'${AZ_PARTNAME}'">.*$\)/\1\n'"${AZ_NODELINE}"'/
			s/\(PartitionName='${AZ_PARTNAME}'.*Nodes=.*\)/\1,'${AZ_NODENAME}'/
			s/=,/=/
			s/,$//
		}'  $conffile
	else
		echo "Node $AZ_NODENAME is already present in partition $AZ_PARTNAME" >&2
	fi
else
	echo "partition not found, creating partition $AZ_PARTNAME with node $AZ_NODENAME" >&2
	cat >> $conffile <<EOF
#<partition name="${AZ_PARTNAME}">
${AZ_NODELINE}
PartitionName=${AZ_PARTNAME} Default=NO MaxTime=4320 Priority=50 State=UP Nodes=${AZ_NODENAME}
#</partition>
EOF

fi
