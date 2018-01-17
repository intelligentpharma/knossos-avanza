#!/bin/bash

#lectura de parametros


function print_help {
    >&2 echo "$0 - script de reconfiguracion de slurm"
    >&2 echo " "
    >&2 echo "$0 [options] [arguments]"
    >&2 echo " "
    >&2 echo "options:"
    >&2 echo "-h, --help                help"
    >&2 echo "-ip                       ip privada"
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


#reconfigrar cluster slurm

/usr/local/bin/scontrol reconfigure
sleep 2
/usr/local/sbin/slurmctld

echo "SUCCESS"
