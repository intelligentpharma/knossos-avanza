
#lectura de parametros


function print_help {
    >&2 echo "$0 - icinga/nagios knossos probe"
    >&2 echo " "
    >&2 echo "$0 [options] [arguments]"
    >&2 echo " "
    >&2 echo "options:"
    >&2 echo "-h, --help                show brief help"
    >&2 echo "-ip,                      ip privada del worker"
    <&2 echo "-name                     uname de la maquina"
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

#quitaer el worker a /etc/hosts
sed -i -e "/^$IP/d" /etc/hosts
#change the config of slurm.conf
sed -i -e "/^NodeName=$NAME/d" /usr/local/etc/slurm.conf
sed -i -e "s/$NAME,//g" /usr/local/etc/slurm.conf
sed -i -e "s/$NAME//g" /usr/local/etc/slurm.conf

#reconfigrar cluster slurm

/usr/local/bin/scontrol reconfigure
sleep 2
/usr/local/sbin/slurmctld

echo "SUCCESS"
