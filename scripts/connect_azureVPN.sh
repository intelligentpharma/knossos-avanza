#!/bin/bash
# run @hidra-01
run_VPN_up=FALSE
echo "starting openvpn for azure connection..."
if [ ! -f /etc/openvpn/azure_vpn.pid ]; then
    run_VPN_up=TRUE
else
    echo "a previous openvpn pid was found"
    VPN_pid=$(cat /etc/openvpn/azure_vpn.pid)
    echo "VPN pid: $VPN_pid"
    if [ "$VPN_pid" == "" ]; then
        run_VPN_up=TRUE
    else
        if [ -d /proc/$VPN_pid ]; then
            echo "OpenVPN is running for azure. if you want to restart it"
            echo "call disconnect_azureVPN.sh first"
        else
            run_VPN_up=TRUE
        fi
    fi
fi

if [ "$run_VPN_up" == "TRUE" ]; then
    vpn_up_result=$(nohup sh -c 'cd /etc/openvpn/; openvpn --config /etc/openvpn/client.conf & echo $! > /etc/openvpn/azure_vpn.pid')
    echo "
command: nohup sh -c 'cd /etc/openvpn/; openvpn --config /etc/openvpn/client.conf & echo \$! > /etc/openvpn/azure_vpn.pid'
"
    sleep 10
fi

