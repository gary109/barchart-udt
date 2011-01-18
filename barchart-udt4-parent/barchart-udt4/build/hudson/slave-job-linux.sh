#!/bin/bash

#
# custom macosx hudson build to fix apple's "think different"
#

#
# must provide maven home via
# http://wiki.hudson-ci.org/display/HUDSON/Tool+Environment+Plugin
#

echo "### home  = $HOME"
echo "### pwd   = $PWD"

echo "### label = $label"
echo "### jdk   = $jdk"

echo "### MAVEN_OPTS=$MAVEN_OPTS"

"$APACHE_MAVEN_3_HOME/bin/mvn" $MVN_CMD_UDT


