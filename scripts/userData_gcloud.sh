#! /bin/bash

#User data Script to be passed when starting AMIs
#@Author igomez, mnunez
#@Date 28-06-2017
function print_help {
    >&2 echo "$0 - startup script for gcloud instances"
    >&2 echo " "
    >&2 echo "$0 [options] [arguments]"
    >&2 echo "ejemplo "
    >&2 echo "$0 -queue gcloud-3"
    >&2 echo "options:"
    >&2 echo "-h, --help                help"
    <&2 echo "-queue                    queue name"
    >&2 echo " "
}

function parse_args {
    while test $# -gt 0; do
                case "$1" in
                        -h|--help)
                                 print_help
                                 exit 0
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

echo $QUEUE

name="`uname -n`"
#queue se tendra que modificar con el nombre de cola correcto para cada experimento
queue=$QUEUE
#change the /etc/hosts with the name of machine
echo "modificando /etc/hosts"
echo "127.0.0.1   localhost localhost.localdomain $name" > /etc/hosts
echo "192.168.42.10   ip-171-31-99-79 hidra-01" >> /etc/hosts

echo "montando /grid"
mount -a
sleep 2

echo "kill munge socket"
rm -rf /usr/local/var/run/munge/munge.socket.2
echo "ejecutando munge"
sudo -u daemon /usr/local/sbin/munged
echo "munge finalizado"
	
rm -f /usr/local/etc/sysconfig/slurm.conf
ln -s /grid/env/slurm/slurm.conf /usr/local/etc/sysconfig/slurm.conf
sed -i -e "s/etc\"/etc\/sysconfig\"/g" /etc/init.d/slurm

echo "starting autoshutdown daemon"
cp /grid/apps/scripts/autoshutdown.sh /tmp/autoshutdown.sh
#nohup /tmp/autoshutdown.sh > /dev/null & 

echo "SUCCESS"
exit
