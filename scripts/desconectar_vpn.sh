#!/bin/bash
#@Author Miguel Nunez
#@Project Avanza


function print_help {
    >&2 echo "$0 - script to stop VPN"
    >&2 echo " "
    >&2 echo "$0 [options] [arguments]"
    >&2 echo "example to stop AWS vpn node"
    >&2 echo "$0 -aws"
    >&2 echo "example to stop Gcloud vpn node"
    >&2 echo "$0 -gcloud"
    >&2 echo "options:"
    >&2 echo "-h, --help                help"
    >&2 echo "-aws                      stop AWS vpn node"
    <&2 echo "-gcloud                   stop Gcloud vpn node"
    >&2 echo " "
}


function parse_args {

        key="$1"

        echo "Parsing"
    case $key in
                        -h|--help)
                                 print_help
                                 exit 0
                                 ;;

                        -aws)
                            echo "AWS"
                            aws ec2 stop-instances --instance-ids i-c823a267
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

    if [ $# == 0 ]; then
           aws
        exit 0
    fi


}


function gcloud {
    echo "entra en funcion google"
    /root/google-cloud-sdk/bin/gcloud compute instances stop vpn &
    exit 0
}


parse_args $@

