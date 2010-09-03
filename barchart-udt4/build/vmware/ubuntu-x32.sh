#!/bin/bash

###############

THIS_PATH="$(dirname $(readlink -f $0))"

###############

# 
source "$THIS_PATH/common.sh"

verify_tool_present "vmrun"
verify_tool_present "vmware"

VM="$HOME/.vmware/ubuntu-x32/ubuntu-x32.vmx"
USER="user1"
PASS="user1"

log "VM=$VM"

vmrun -T ws start "$VM" nogui
verify_run_status "$?" "vm start"

vmrun -T ws -gu "$USER" -gp "$PASS" runScriptInGuest "$VM" "/bin/bash" "ls -las"
verify_run_status "$?" "vm run"

vmrun -T ws stop "$VM" soft
verify_run_status "$?" "vm stop"
