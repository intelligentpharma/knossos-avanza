#!/bin/bash

#lectura de parametros


function print_help {
    >&2 echo "$0 - script de reconfiguracion de slurm"
    >&2 echo " "
    >&2 echo "$0 [options] [arguments]"
    >&2 echo "ejemplo "
    >&2 echo "$0 -ip 172.31.17.46,172.31.17.47,172.31.17.45 -name ip-172-31-17-46,ip-172-31-17-47,ip-172-31-17-45 -queue aws-1"
    >&2 echo "options:"
    >&2 echo "-h, --help                help"
    >&2 echo "-ip                       ip privada, van separadas por comas"
    <&2 echo "-name                     uname -n de la instancia, va separada por comas"
    <&2 echo "-queue                    nombre de la cola"
    <&2 echo "-gcloud                    usar google cloud"
    >&2 echo " "
}

function parse_args {
    while test $# -gt 0; do
                case "$1" in
                        -h|--help)
                                 print_help
                                 exit 0
                                 ;;
                        -gcloud)
                               shift
                               gcloud=1
                               shift
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
                         -queue)
                                shift
                                 if test $# -gt 0; then
                                     export QUEUE=$1
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

if [ $gcloud = 1 ]; then


    #check VPN connection
    /root/avanza/conectar_vpn.sh -gcloud

    #echo "la ip privada es $IP"
    #echo "el uname es $NAME"

    #crear el array de vasriables
    IP_ARRAY=(`echo $IP | sed -e "s/,/ /g"`)
    NAME_ARRAY=(`echo $NAME | sed -e "s/,/ /g"`)

    #echo "el primer elemento es  ${IP_ARRAY[0]}"

    #echo ${#IP_ARRAY[@]}
    #echo ${#NAME_ARRAY[@]}

    sed -i -e "s/^#agregar-cola/PartitionName=$QUEUE Nodes= Default=NO MaxTime=4320 Priority=50 State=UP\n#agregar-cola/g" /grid/env/slurm/slurm.conf

    echo "${IP_ARRAY[0]}  ${NAME_ARRAY[0]}" >> /etc/hosts
    sed -i -e "s/^#agregar-worker/NodeName=${NAME_ARRAY[0]} Sockets=2 CoresPerSocket=1 ThreadsPerCore=1 RealMemory=1701 State=IDLE\n#agregar-worker/g" /grid/env/slurm/slurm.conf
    sed -i -e "s/PartitionName=$QUEUE Nodes=/PartitionName=$QUEUE Nodes=${NAME_ARRAY[0]}/g" /grid/env/slurm/slurm.conf


    x=1
    while [ $x -lt ${#NAME_ARRAY[@]} ]
    do
           #gregar el worker a /etc/hosts
           echo "${IP_ARRAY[$x]}  ${NAME_ARRAY[$x]}" >> /etc/hosts

          #change the config of slurm.conf
          sed -i -e "s/^#agregar-worker/NodeName=${NAME_ARRAY[$x]} Sockets=2 CoresPerSocket=1 ThreadsPerCore=1 RealMemory=1701 State=IDLE\n#agregar-worker/g" /grid/env/slurm/slurm.conf
          sed -i -e "s/PartitionName=$QUEUE Nodes=/PartitionName=$QUEUE Nodes=${NAME_ARRAY[$x]},/g" /grid/env/slurm/slurm.conf

          x=$(( $x + 1 ))
   done


else

    #check VPN connection
    /root/avanza/conectar_vpn.sh

    #echo "la ip privada es $IP"
    #echo "el uname es $NAME"

    #crear el array de variables
    IP_ARRAY=(`echo $IP | sed -e "s/,/ /g"`)
    NAME_ARRAY=(`echo $NAME | sed -e "s/,/ /g"`)

    #echo "el primer elemento es  ${IP_ARRAY[0]}"

    #echo ${#IP_ARRAY[@]}
    #echo ${#NAME_ARRAY[@]}

    sed -i -e "s/^#agregar-cola/PartitionName=$QUEUE Nodes= Default=NO MaxTime=4320 Priority=50 State=UP\n#agregar-cola/g" /grid/env/slurm/slurm.conf

    echo "${IP_ARRAY[0]}  ${NAME_ARRAY[0]}" >> /etc/hosts
    sed -i -e "s/^#agregar-worker/NodeName=${NAME_ARRAY[0]} Sockets=2 CoresPerSocket=2 ThreadsPerCore=1 RealMemory=7482 State=IDLE\n#agregar-worker/g" /grid/env/slurm/slurm.conf
    sed -i -e "s/PartitionName=$QUEUE Nodes=/PartitionName=$QUEUE Nodes=${NAME_ARRAY[0]}/g" /grid/env/slurm/slurm.conf


    x=1
    while [ $x -lt ${#NAME_ARRAY[@]} ]
    do
            #gregar el worker a /etc/hosts
            echo "${IP_ARRAY[$x]}  ${NAME_ARRAY[$x]}" >> /etc/hosts

            #change the config of slurm.conf
            sed -i -e "s/^#agregar-worker/NodeName=${NAME_ARRAY[$x]} Sockets=2 CoresPerSocket=2 ThreadsPerCore=1 RealMemory=7482 State=IDLE\n#agregar-worker/g" /grid/env/slurm/slurm.conf
            sed -i -e "s/PartitionName=$QUEUE Nodes=/PartitionName=$QUEUE Nodes=${NAME_ARRAY[$x]},/g" /grid/env/slurm/slurm.conf

            x=$(( $x + 1 ))
    done


fi

#reconfigrar cluster slurm

/usr/local/bin/scontrol reconfigure
sleep 2
/usr/local/sbin/slurmctld
#systemctl restart slurm
sleep 2
#/usr/local/bin/scontrol update nodename=${NAME_ARRAY[0]} state=resume

echo "SUCCESS"
