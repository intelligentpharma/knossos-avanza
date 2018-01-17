# Author:		ggutierrez @ Intelligent Pharma - 2017
# Description:	helper functions for azure's slurm partition and nodes management


# az_delnode <NodeName> <PartitionName> [<config file=/grid/env/slurm/slurm.conf>]
# 	removes a node from a partition and its info from the partition block
	if [ ${#@} -lt 2 ]; then
		echo "not enough arguments" >&2
		exit
	fi
	export conffile=${3:-'/grid/env/slurm/slurm.conf'}
	AZ_PARTNAME=$2
	AZ_NODENAME=$1
	sed -i -e '/^#<partition name="'${AZ_PARTNAME}'">/,/#<\/partition>/ {
		/NodeName='${AZ_NODENAME}'.*/d
		s/\(Partition.*[=,]\)'$AZ_NODENAME'\(.*\)/\1\2/
		s/,,/,/g
		s/=,/=/
		s/,$//g
	}' ${conffile}
