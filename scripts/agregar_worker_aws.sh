#!/bin/bash
#@ Author Miguel Nunez
#@ Project Avanza


#check VPN connection
/root/avanza/conectar_vpn.sh

#lanza una instancia nueva en amazon
WORKER=`aws ec2 run-instances --image-id ami-d64894b6  --count 1 --instance-type c4.large --subnet-id subnet-8c79e9e9`
#WORKER=`aws ec2 run-instances --image-id ami-d64894b6  --count 1 --instance-type t2.medium --subnet-id subnet-8c79e9e9`



#WORKER=`cat /root/avanza/worker.txt`

#recogida de datos del worker


IP=`echo $WORKER | cut -d'"' -f44 `
IDS=`echo $WORKER | cut -d'"' -f58 `
#IDS="i-a1b812b9"
NAME=`echo $IP  | sed -e "s/\./-/g" | sed -e "s/^/ip-/g"`


echo "la ip privada es $IP"
echo "el uname es $NAME"
echo "el ids de la instancia es $IDS"

#agregar el worker a /etc/hosts
echo "$IP   $NAME" >> /etc/hosts

#change the config of slurm.conf
sed -i -e "s/^#agregar-worker/NodeName=$NAME Sockets=1 CoresPerSocket=2 ThreadsPerCore=1 RealMemory=3954 State=IDLE\n#agregar-worker/g" /usr/local/etc/slurm.conf
sed -i -e "s/^PartitionName=unlimited Nodes=/PartitionName=unlimited Nodes=$NAME,/g" /usr/local/etc/slurm.conf
sed -i -e "s/^PartitionName=short Nodes=/PartitionName=short Nodes=$NAME,/g" /usr/local/etc/slurm.conf




#aws ec2 describe-instances --instance-ids i-ced77dd6


### Comprueba que la instancia este running


STATE=`aws ec2 describe-instance-status --include-all-instances --instance-ids $IDS`
STATUS=`echo $STATE | cut -d'"' -f24`

echo "el status es $STATUS"


while [ "$STATUS" != "ok" ] ; do
            sleep 5
            STATE=`aws ec2 describe-instance-status --include-all-instances --instance-ids $IDS`
            STATUS=`echo $STATE | cut -d'"' -f24`
            CODE=`echo $STATE | cut -d'"' -f11`
            echo "dentro del bucle code $STATUS"
done

INSTANCE=`aws ec2 describe-instances --instance-ids $IDS`
DNS=`echo $INSTANCE | cut -d'"' -f24`

echo "el dns es $DNS"

#reconfigurar worker
#ssh root@ec2-54-68-55-240.us-west-2.compute.amazonaws.com /root/configure_node.sh

ssh -oStrictHostKeyChecking=no -oUserKnownHostsFile=/dev/null root@$DNS -t 'nohup /root/configure_node.sh'
ssh -oStrictHostKeyChecking=no -oUserKnownHostsFile=/dev/null root@$DNS -t 'sed -i -e "s/grid_test/grid/g" /etc/fstab'
ssh -oStrictHostKeyChecking=no -oUserKnownHostsFile=/dev/null root@$DNS -t 'mount -a'

#reconfigrar cluster slurm
/usr/local/bin/scontrol reconfigure
/usr/local/sbin/slurmctld


echo "SUCCESS"
