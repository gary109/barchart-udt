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
LIB_FOLDER="$PWD/../target/test-classes"

log "current folder : $PWD"

case $KIND in
	start)
		log "pre-build task;"
		log "done"
	;;
	finish)
		log "post-build task;"
		cp -f -v *.so *.dll *.jnilib "$LIB_FOLDER"
		log "done"
	;;
	*)
		log "error; unecpected KIND=$KIND"
		exit 1
	;;
esac

log "########################################"
