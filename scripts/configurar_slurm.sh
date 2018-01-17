#!/bin/bash

#lectura de parametros


function print_help {
    >&2 echo "$0 - script de confioguracion de un nodo nuevo nodo worker de aws en slurm"
    >&2 echo " "
    >&2 echo "$0 [options] [arguments]"
    >&2 echo " "
    >&2 echo "options:"
    >&2 echo "-h, --help                help"
    >&2 echo "-ip                       ip privada"
    >&2 echo "-ids                      identificador de instancia"
    >&2 echo "-dns                      dns publico"
    <&2 echo "-name                     uname -n de la instancia"
    >&2 echo " "
}

function parse_args {
    while test $# -gt 0; do
                case "$1" in
                        -h|--help)
                                 print_help
                                 exit 0
                                 ;;
                        -ip)
                               shift
                               if test $# -gt 0; then
                                         export IP=$1
                               else
                                         echo "No name specified."
                                         exit 1
                               fi
                               shift
                               ;;

                        -ids)
                               shift
                               if test $# -gt 0; then
                                         export IDS=$1
                               else
                                         echo "No name specified."
                                         exit 1
                               fi
                               shift
                               ;;

                           -dns)
                               shift
                               if test $# -gt 0; then
                                        export DNS=$1
                               else
                                        echo "No DNS specified."
                                        exit 1
                               fi
                               shift
                               ;;

                        -name)
                                shift
                                 if test $# -gt 0; then
                                     export NAME=$1
                                else
                                        echo "No Name specified"
                                        exit 1
                                fi
                                shift
                                ;;
                        *)
                           break
                           ;;
                esac
    done
}


#recogida de datos del worker

parse_args $@

echo "la ip privada es $IP"
echo "el uname es $NAME"
echo "el ids de la instancia es $IDS"

#agregar el worker a /etc/hosts
echo "$IP   $NAME" >> /etc/hosts

#change the config of slurm.conf
sed -i -e "s/^#agregar-worker/NodeName=$NAME Sockets=1 CoresPerSocket=2 ThreadsPerCore=1 RealMemory=3954 State=IDLE\n#agregar-worker/g" /usr/local/etc/slurm.conf
sed -i -e "s/^PartitionName=unlimited Nodes=/PartitionName=unlimited Nodes=$NAME,/g" /usr/local/etc/slurm.conf
sed -i -e "s/^PartitionName=short Nodes=/PartitionName=short Nodes=$NAME,/g" /usr/local/etc/slurm.conf


### Comprueba que la instancia este running


STATE=`aws ec2 describe-instance-status --include-all-instances --instance-ids $IDS`
STATUS=`echo $STATE | cut -d'"' -f24`

echo "el status es $STATUS"


while [ "$STATUS" != "ok" ] ;
    do
                sleep 5
                STATE=`aws ec2 describe-instance-status --include-all-instances --instance-ids $IDS`
                STATUS=`echo $STATE | cut -d'"' -f24`
                CODE=`echo $STATE | cut -d'"' -f11`
                echo "dentro del bucle code $STATUS"
    done

#INSTANCE=`aws ec2 describe-instances --instance-ids $IDS`
#DNS=`echo $INSTANCE | cut -d'"' -f24`

echo "el dns es $DNS"
sleep 5

#reconfigurar worker

#ssh -oStrictHostKeyChecking=no -oUserKnownHostsFile=/dev/null root@$DNS -t 'nohup /root/configure_node.sh'
ssh -oStrictHostKeyChecking=no -oUserKnownHostsFile=/dev/null root@$DNS -t "nohup /root/configure_node.sh"
ssh -oStrictHostKeyChecking=no -oUserKnownHostsFile=/dev/null root@$DNS -t 'sed -i -e "s/grid_test/grid/g" /etc/fstab'
ssh -oStrictHostKeyChecking=no -oUserKnownHostsFile=/dev/null root@$DNS -t 'mount -a'

#reconfigrar cluster slurm

/usr/local/bin/scontrol reconfigure
sleep 2
/usr/local/sbin/slurmctld

echo "SUCCESS"
