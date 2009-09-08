#!/bin/bash

# used by eclipse cdt interactive builder

KIND=$1

LIB_NAME="SocketUDT"
LIB_FOLDER="$PWD/../src/main/resources"

ARTIFACT=""
ARTIFACT_MAP=""

LIB_FILE=""

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
findArtifact(){
	COUNT=`ls -1 *.so | wc -l`
	if [ $COUNT = 1 ] ; then
		ARTIFACT=`ls -1 *.so`
	else
		log "error; can not find artifact; COUNT=$COUNT"
		exit 1
	fi 		
}
findArtifactMap(){
	COUNT=`ls -1 *.map | wc -l`
	if [ $COUNT = 1 ] ; then
		ARTIFACT_MAP=`ls -1 *.map`
	else
		log "error; can not find artifact map; COUNT=$COUNT"
		exit 1
	fi 		
}
makeLibraryName() {
	OS=`uname -s`
	OS=`toLower $OS`
	MACH=`uname -m`
	MACH=`toLower $MACH`
	case $OS in
		linux)
			case $MACH in
				i386)
					LIB_FILE="lib$LIB_NAME-linux-x86-32.so"
				;;
				x86_64)
					LIB_FILE="lib$LIB_NAME-linux-x86-64.so"
				;;
				*)
					log "error; not supported MACH=$MACH"
					exit 1
				;;
			esac		
		;;
		*)
			log "error; not supported OS=$OS"
			exit 1
		;;
	esac
}

case $KIND in
	start)
		log "pre-build task;"
		log "done"
	;;
	finish)
		#
		log "post-build task;"
		findArtifact
		findArtifactMap
		makeLibraryName
		#
		MAP_FILE="$LIB_FILE.map"
		TARGET="$LIB_FOLDER/$LIB_FILE"
		TARGET_MAP="$LIB_FOLDER/$MAP_FILE"
		#
		log "ARTIFACT     : $ARTIFACT"
		log "ARTIFACT_MAP : $ARTIFACT_MAP"
		log "TARGET     : $TARGET"
		log "TARGET_MAP : $TARGET_MAP"
		#
		cp --force --update "$ARTIFACT" "$TARGET" 
		cp --force --update "$ARTIFACT_MAP" "$TARGET_MAP" 
		ldd -r "$TARGET"
		ls -l "$LIB_FOLDER"
		#
		log "done"
	;;
	*)
		log "error; unecpected KIND=$KIND"
		exit 1
	;;
esac

exit 0
