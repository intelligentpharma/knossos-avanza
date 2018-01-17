#!/bin/bash
#@Author Miguel Nunez
#@Project Avanza


function print_help {
    >&2 echo "$0 - script to connect VPN"
    >&2 echo " "
    >&2 echo "$0 [options] [arguments]"
    >&2 echo "example to connect to amazon AWS "
    >&2 echo "$0 -aws"
    >&2 echo "example to connect to Google Cloud Platform "
    >&2 echo "$0 -gcloud"
    >&2 echo "options:"
    >&2 echo "-h, --help                help"
    >&2 echo "-aws                      connect VPN to aws"
    <&2 echo "-gcloud                   connect VPN to google cloud platform"
    >&2 echo " "
}


function parse_args {
    while test $# -gt 0; do
                case "$1" in
                        -h|--help)
                                 print_help
                                 exit 0
                                 ;;

                        -aws)
#                               echo "entra en funcion parse"
                                aws
                                exit 0
                                ;;
                        -gcloud)
                                gcloud
                                exit 0
                                ;;
                        *)
                                exit 0
                                ;;


                esac                                                  
    done

    if [ $# == 0 ]; then
           aws
        exit 0
    fi


}

function aws {
#    echo "entra en funcion aws"

    vpn=`ifconfig | grep -c 172.31.26.200`

    #echo $vpn

    if [[ "$vpn" == "1" ]]; then
        echo "AWS VPN connected"
        exit 0

    else
#        echo "entra en else"

        #enciende el nodo VPN en amazon
        /bin/aws ec2 start-instances --instance-ids i-c823a267
        sleep 30
        /sbin/pppd call vpnconnection1
        sleep 5
        #edit route
        network=`ifconfig | grep -B 1 172.31.26.200 | grep ppp | cut -d':' -f 1`
        /sbin/route add -net 172.31.16.0/20 dev $network
        ifconfig $network mtu 1400

    fi
    exit 0
}


function gcloud {

    vpn=`ifconfig | grep -c 192.168.42.10`

    if [[ $vpn == 1 ]]; then
        echo "Google Cloud VPN connected"
        exit 0

    else

        #starts Google Cloud VPN node
        /root/google-cloud-sdk/bin/gcloud compute instances start vpn vpn --zone us-central1-a
        sleep 30
        strongswan up myvpn 
        echo "c myvpn" > /var/run/xl2tpd/l2tp-control
        sleep 5
        network=`ifconfig | grep -B 1 192.168.42.10 | grep ppp | cut -d':' -f 1`
        /sbin/route add -net 10.128.0.0/20 dev $network
        #ifconfig ppp0 mtu 1400

    fi
    exit 0
}


parse_args $@

