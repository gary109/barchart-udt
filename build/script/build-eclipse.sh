#!/bin/sh

# used by eclipse cdt interactive builder

SCRIPT=`basename $0`

log() {
	echo "$SCRIPT: $1"
}

toLower() {
  echo $1 | tr "[:upper:]" "[:lower:]"
}

toUpper() {
  echo $1 | tr "[:lower:]" "[:upper:]"
}

log "########################################"

KIND=$1

log "mode : $KIND"
log "current folder : $PWD"

LIB_NAME="SocketUDT"
LIB_FOLDER="$PWD/../target/classes"

cp -f -v *"$LIB_NAME"* "$LIB_FOLDER"

log "available libraries : "

ls -las *"$LIB_NAME"*

log "########################################"
