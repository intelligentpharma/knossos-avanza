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

                        *)
                           break
                           ;;
                esac
    done
}


#recogida de datos del worker

parse_args $@


echo "el dns es $DNS"

#reconfigurar worker

#ssh -oStrictHostKeyChecking=no -oUserKnownHostsFile=/dev/null root@$DNS -t 'nohup /root/configure_node.sh'
ssh -oStrictHostKeyChecking=no -oUserKnownHostsFile=/dev/null root@$DNS -t '/usr/local/sbin/slurmctld'
sleep 5
ssh -oStrictHostKeyChecking=no -oUserKnownHostsFile=/dev/null root@$DNS -t '/usr/local/sbin/slurmd'

echo "SUCCESS"
