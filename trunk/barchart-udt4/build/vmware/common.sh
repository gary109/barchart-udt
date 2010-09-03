#!/bin/bash

if [ "$THIS_PATH" == "" ] ; then
	echo "fatal: THIS_PATH must be set"
	exit 1
fi

# name of this script
THIS_FILE="$(basename $0)"


# log destination
THIS_LOG="$THIS_PATH/$THIS_FILE.log"

function log {
	local MESSAGE="$1"
	echo "### $MESSAGE"
}
log "PATH=$PATH"
log "THIS_PATH=$THIS_PATH"
log "THIS_FILE=$THIS_FILE"


function verify_root_user {

	if [ "$(id -u)" != "0" ]; then
	   echo "fatal: script must be run as root"
	   exit 1
	fi

}

function verify_tool_present {
	
	local TOOL="$1"

	which "$TOOL" > /dev/null
	if [ "$?" != "0" ]; then
		log "fatal: tool $TOOL must be installed"
		exit 1
	else
		log "found tool: $TOOL"
	fi

}
