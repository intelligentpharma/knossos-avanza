#!/bin/bash
# run @client
# after updating /etc/exportfs @nfs-server (hidra-01)
# and systemctl reload nfs-server


######################
# install nfs client #
######################

apt install -y nfs-client

#############################
# setup configuration files #
#############################

# check for hidra-01 entry in /etc/hosts
if [ "$(cat /etc/hosts | grep hidra-01)" == "" ]; then
    echo "192.168.100.200	hidra-01" >> /etc/hosts
fi

mkdir /grid

if [ "$(grep hidra-01:/srv/nfs/grid /etc/fstab )" == "" ]; then
    echo 'hidra-01:/srv/nfs/grid          /grid           nfs     rw,hard,intr,sync,vers=3,retrans=4,rsize=8192,wsize=8192 0 0' >> /etc/fstab
fi

########################
# mount the filesystem #
########################

ping -c2 hidra-01
result="$?"
if [ $result == 0 ]; then
    mount -a
fi
