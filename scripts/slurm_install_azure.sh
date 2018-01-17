#!/bin/bash
# run @nodes
# slurm install/deploy script
# requires vpn connection
# requires hidra-01 online
# requires nfs /grid

#################
# install munge #
#################

# install
apt install -y libmunge-dev libmunge2 munge

# copy munge.key
cp /grid/env/munge/munge.key /etc/munge/munge.key
chown munge:munge /etc/munge/munge.key
chmod 400 /etc/munge/munge.key

# set up munge in etcpasswd
if [ "$(grep ^munge /etc/passwd)" != "" ]; then
	sed -i -e 's@^munge.*@munge:x:501:501::/var/run/munge;/sbin/nologin@' /etc/passwd
else
	echo 'munge:x:501:501::/var/run/munge;/sbin/nologin' >> /etc/passwd
fi

chown munge.munge /var/log/munge /run/munge /var/lib/munge /etc/munge  -R

systemctl restart munge

########################
# install slurm daemon #
########################

#apt install -y slurmd
#ln -s /grid/env/slurm/slurm.conf /etc/slurm-llnl/slurm.conf

for i in {bin,lib,sbin,etc}; do
    mkdir /usr/local/$i -p
    cp -r /grid/env/slurm-14.11.8/$i/* /usr/local/$i/
done	

groupadd -g 64030 slurm
useradd -u 64030 -s /bin/false slurm


