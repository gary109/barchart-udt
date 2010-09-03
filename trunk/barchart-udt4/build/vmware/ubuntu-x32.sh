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

HOME_GUEST="/home/$USER"

JDK_DIR="jdk1.6.0_21"
JDK_BIN="jdk-6u21-linux-i586.bin"

JDK_BIN_HOST="/$HOME/downloads/$JDK_BIN"
JDK_BIN_GUEST="$HOME_GUEST/$JDK_BIN"


VMRUN_EXISTS="vmrun -T ws -gu $USER -gp $PASS fileExistsInGuest $VM"
VMRUN_SCRIPT="vmrun -T ws -gu $USER -gp $PASS runScriptInGuest $VM"
VMRUN_PROGRAM="vmrun -T ws -gu $USER -gp $PASS runProgramInGuest $VM"
VMRUN_COPY_HOST_GUEST="vmrun -T ws -gu $USER -gp $PASS copyFileFromHostToGuest $VM"
VMRUN_COPY_GUEST_HOST="vmrun -T ws -gu $USER -gp $PASS copyFileFromGuestToHost $VM"

###

vmrun -T ws start "$VM" nogui
verify_run_status "$?" "vm start"

###

$VMRUN_EXISTS "$JDK_BIN_GUEST"
if [ "$?" == "0" ]; then
	log "java found"
else
	#
	$VMRUN_COPY_HOST_GUEST "$JDK_BIN_HOST" "$JDK_BIN_GUEST"
	verify_run_status "$?" "vm jdk copy"
	#
	$VMRUN_SCRIPT "/bin/bash" "cd $HOME_GUEST; ./$JDK_BIN"
	verify_run_status "$?" "vm jdk install"
	#
	log "java installed"
fi

###

$VMRUN_SCRIPT "/bin/bash" "ls -las"
verify_run_status "$?" "vm run"

###

vmrun -T ws stop "$VM" soft
verify_run_status "$?" "vm stop"

###