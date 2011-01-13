#/bin/sh

PROJ="barchart-udt4"

BASE="$WORKSPACE/$PROJ"
SITE="$WORKSPACE/site"

echo "### BASE=$BASE"
echo "### SITE=$SITE"

cd "$SITE"

svn delete --force "$PROJ"


