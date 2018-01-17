#!/bin/bash


function print_help {
    >&2 echo "$0 - script de reconfiguracion de slurm eliminar cola y sus workers"
    >&2 echo " "
    >&2 echo "$0 [options] [arguments]"
    >&2 echo "ejemplo "
    >&2 echo "$0 -names ip-172-31-17-46,ip-172-31-17-47,ip-172-31-17-45 -queue aws-1"
    >&2 echo "options:"
    >&2 echo "-h, --help                help"
    >&2 echo "-ip                       ip privada, van separadas por comas"
    <&2 echo "-names                     uname -n de la instancia, va separada por comas"
    <&2 echo "-queue                    nombre de la cola"
    >&2 echo " "
}

function parse_args {
    while test $# -gt 0; do
                case "$1" in
                        -h|--help)
                                 print_help
                                 exit 0
                                 ;;

                        -names)
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

#crear el array de vasriables
NAME_ARRAY=(`echo $NAME | sed -e "s/,/ /g"`)



sed -i -e "/${NAME_ARRAY[0]}/ d" /etc/hosts
sed -i -e "/^NodeName=${NAME_ARRAY[0]}/ d" /grid/env/slurm/slurm.conf


x=1
while [ $x -lt ${#NAME_ARRAY[@]} ]
do
        #quitar el worker a /etc/hosts
        sed -i -e "/${NAME_ARRAY[$x]}/ d" /etc/hosts

        #delete the worker in slurm.conf
        sed -i -e "/^NodeName=${NAME_ARRAY[$x]}/ d" /grid/env/slurm/slurm.conf

        x=$(( $x + 1 ))
done

sed -i -e "/PartitionName=$QUEUE/ d" /grid/env/slurm/slurm.conf

#reconfigrar cluster slurm

/usr/local/bin/scontrol reconfigure
sleep 2
#/usr/local/sbin/slurmctld
systemctl restart slurm


echo "SUCCESS"
