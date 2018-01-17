#!/bin/bash
# run @client
# this script was run over debian version 'whoknows' for azure (actually it seems to be jessie)
# set VPNServerIP with actual VPN server public ip address
# invocation is:
# ./openvpn_azure_client.sh [<ip address>]
# where <ip address> is the address for VPN Server.

# for some reason, this is not working properly
# so i'll hardcode the address for now
export VPNServerIP="${1:51.141.50.204}"

###################
# install openvpn #
###################

apt install openvpn -y

# this files should have been created by server
#/etc/openvpn/ca.crt
#/etc/openvpn/ta.key
#/etc/openvpn/easy-rsa/keys/client.crt (Where hostname is the hostname of the client).
#/etc/openvpn/easy-rsa/keys/client.key (Where hostname is the hostname of the client).
# and be copied to /etc/openvpn/

###########################
# create client.conf file #
###########################

cd /etc/openvpn
cat > client.conf <<EOF
client
dev tap
proto tcp
#remote ${VPNServerIP} 1194
remote 51.141.50.204 1194
resolv-retry infinite
nobind
#user nobody
#group nogroup
persist-key
persist-tun
ca ca.crt
cert /etc/openvpn/azure_client.crt
key /etc/openvpn/azure_client.key
tls-auth ta.key 1
comp-lzo
verb 6
log-append openvpn.log
EOF

########################
# bring the service up #
########################

# enable it, just in case
systemctl enable openvpn

# and restart, to work with the new configuration
systemctl restart openvpn
echo "if it doesnt work, use

systemctl stop openvpn
/root/scripts/connect_azureVPN.sh
this will daemonize the openvpn in client mode
"

