#!/bin/bash

#lectura de parametros


function print_help {
    >&2 echo "$0 - check ssh connections"
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

ssh -oStrictHostKeyChecking=no -oUserKnownHostsFile=/dev/null root@$DNS -t "nohup /root/hello.sh"

echo "SUCCESS"
