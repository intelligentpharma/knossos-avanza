# Author:		ggutierrez @ Intelligent Pharma - 2017
# Description:	helper functions for azure's slurm partition and nodes management

# az_delpartition <PartitionName> [<config file=/grid/env/slurm/slurm.conf>]
#	removes a whole partition block
	export conffile=${2:-'/grid/env/slurm/slurm.conf'}
	if [ ${#@} -lt 1 ]; then
		echo "not enough arguments" >&2
		exit
	fi
	AZ_PARTNAME=$1
	sed -i -e '/^#<partition name="'${AZ_PARTNAME}'">/,/#<\/partition>/ { d }' ${conffile}

