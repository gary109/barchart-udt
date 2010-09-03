#!/bin/bash

###############

THIS_PATH="$(dirname $0 | cd && pwd)"

###############

# 
source "$THIS_PATH/common.sh"

verify_tool_present "vmrun"

echo "" > env.txt
echo $HOME >> env.txt

export VM="$HOME/.vmware/ubuntu-x32/ubuntu-x32.vmx"

vmrun -T ws start "$VM" nogui
vmrun -T ws -gu user1 -gp user1 runScriptInGuest "$VM" "/bin/bash" "ls -las"
vmrun -T ws stop "$VM" soft
