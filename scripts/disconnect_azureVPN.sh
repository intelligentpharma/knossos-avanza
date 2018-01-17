#!/bin/bash
# run @hidra-01
if [ -f /etc/openvpn/azure_vpn.pid ] && [ "$(cat /etc/openvpn/azure_vpn.pid)" != "" ] && [ -d /proc/$(cat /etc/openvpn/azure_vpn.pid) ] ; then
    PID=$(cat /etc/openvpn/azure_vpn.pid )
    echo "stopping previous openvpn client"
    echo "PID: $PID"
    kill $PID
    rm /etc/openvpn/azure_vpn.pid
    sleep 1
    if [ ! -d /proc/$PID ]; then
        echo "Success"
    else
        echo "Error. process $PID still exists"
    fi
else
    echo "there is no azure vpn connection registered"
    echo "if you didn't expect that, check /etc/openvpn/openvpn.log"
    echo "for further information"
    VPN_search_output=$(ps aux | grep openvpn | grep -v grep )
    if [ "$VPN_search_output" != "" ]; then
        echo "but I found this"
        ps aux | grep openvpn | grep -v grep
        echo "I hope these are the droids you're looking for"
    fi
fi
