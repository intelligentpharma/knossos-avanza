#!/bin/bash
export AVANZAHOME=${AVANZAHOME:-~/avanza}
if [ "$1" == "" ]; then
    echo "Error: No destination host" >&2
    exit 1
fi
ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ${1} 'mkdir ~/azure/keys -p'
scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ${1}  $AVANZAHOME/azure/keys/azure_client* $AVANZAHOME/azure/keys/ta.key $AVANZAHOME/azure/keys/ca.crt ${1}:
~/azure/keys/