#!/bin/bash
# run @server
# this script was run over debian version 'whoknows' for azure (actually it seems to be jessie)

##################################################################
# install openvpn y bridge utils & dependencies such as easy-rsa #
##################################################################

apt install openvpn bridge-utils -y
# ifconfig package not required on stretch


###########################
# set up bridge interface #
###########################

if [ "$(cat /etc/network/interfaces | grep 'iface br0 inet static')" == "" ]; then

cat >> /etc/network/interfaces <<EOF
auto br0
iface br0 inet static
        address 192.168.100.10
        network 192.168.100.0
        netmask 255.255.255.0
        broadcast 192.168.100.255
        gateway 192.168.100.1
        bridge_ports eth0
        bridge_fd 9
        bridge_hello 2
        bridge_maxage 12
        bridge_stp off
EOF
fi

systemctl restart networking



#######################
# create certificates #
#######################

# copy easy-rsa scripts to the working directory
mkdir /etc/openvpn/easy-rsa -p
cd /etc/openvpn/easy-rsa
cp /usr/share/easy-rsa/* ./

# load env vars and check if the info is properly
# set up for Intelligent Pharma
# if it is not, then update the file and re-source
source vars
if [ "$KEY_COUNTRY" != "ES" ]; then
cat >> vars <<EOF
export KEY_COUNTRY="ES"
export KEY_PROVINCE="B"
export KEY_CITY="Barcelona"
export KEY_ORG="IntelligentPharma"
export KEY_EMAIL=""
export KEY_OU="CSS"
export KEY_CONFIG=/etc/openvpn/easy-rsa/openssl-1.0.0.cnf
EOF

source vars
fi

# clean previous keys and certs
./clean-all

# generate pems, certs, etc.
./build-dh
./pkitool --initca
./pkitool --server server
openvpn --genkey --secret ta.key
# move them to /etc/openvpn
cp ta.key /etc/openvpn
cd keys
cp server.crt server.key ca.crt dh2048.pem /etc/openvpn/


# create client certificates
cd /etc/openvpn/easy-rsa
if [ ! -f keys/hidra-01.key ]; then
	source vars
    ./pkitool hidra-01
fi

if [ ! -f keys/azure_client.key ]; then
	source vars
	./pkitool azure_client
fi

# copy them all to the client (requires passwordless auth)
# the client files will be in the client system image
#scp /etc/openvpn/ca.crt /etc/openvpn/ta.key /etc/openvpn/easy-rsa/keys/${newhost}.crt /etc/openvpn/easy-rsa/keys/${newhost}.key $newhost:/etc/openvpn

################################
# openvpn server configuration #
################################


# create server.conf
cd /etc/openvpn
cat > server.conf <<EOF
mode server
tls-server
port 1194
proto tcp
dev tap0
client-config-dir ccd/
duplicate-cn
ca ca.crt
cert server.crt
key server.key  # This file should be kept secret
dh dh2048.pem
server-bridge 192.168.100.101 255.255.255.0 192.168.100.105 192.168.100.200
push "route 192.168.100.0 255.255.255.0"
keepalive 10 120
tls-auth ta.key 0 # This file is secret
comp-lzo
user nobody
group nogroup
persist-key
persist-tun
status openvpn-status.log
log-append openvpn.log
verb 6
script-security 2
client-to-client
up "/etc/openvpn/up.sh br0"
down "/etc/openvpn/down.sh br0"
EOF

# create up.sh
cd /etc/openvpn
cat > up.sh <<EOF
#!/bin/sh
BR=\$1
DEV=\$2
MTU=\$3
ifconfig \$DEV mtu \$MTU promisc up
brctl addif \$BR \$DEV
EOF
sudo chmod 755 up.sh

# create down.sh
cd /etc/openvpn
cat > down.sh <<EOF
#!/bin/sh
BR=\$1
DEV=\$2
brctl delif \$BR \$DEV
ifconfig \$DEV down
EOF
sudo chmod 755 down.sh

# add a config file to set hidra-01 ip as static
mkdir /etc/openvpn/ccd -p
cd /etc/openvpn/ccd
echo "ifconfig-push 192.168.100.200 255.255.255.0" > hidra-01

systemctl restart openvpn
# actually, it may be better to reboot the whole machine.

# important thing, add an ip route so we can ping hidra as 172.26.16.101 with
# ip route add 172.26.16.0 via 192.168.100.200 dev br0
