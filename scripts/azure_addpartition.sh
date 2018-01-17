# Author:		ggutierrez @ Intelligent Pharma - 2017
# Description:	helper functions for azure's slurm partition and nodes management


# az_addpartition <PartitionName> [<config file=/grid/env/slurm/slurm.conf>]
#	creates a new partition block with the partition description
#	the Nodes property is empty, and this creates an invalid slurm.conf file
#	az_addnode or az_delpartition must be called before reconfiguring slurm
	export conffile=${2:-'/grid/env/slurm/slurm.conf'}
    if [ "$(cat $conffile | grep '^#<partition.*name.*=.*"'$1'".*>')" != "" ]; then
		echo "partition $1 already exists"
		exit
	fi
	cat >> $conffile <<EOF
#<partition name="$1">
PartitionName=$1 Default=NO MaxTime=4320 Priority=50 State=UP Nodes=
#</partition>
EOF
