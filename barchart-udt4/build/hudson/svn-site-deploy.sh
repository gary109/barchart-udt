#/bin/sh

PROJ="barchart-udt4"

BASE="$WORKSPACE/$PROJ"
SITE="$WORKSPACE/site/$PROJ"

echo "### BASE=$BASE"
echo "### SITE=$SITE"

svn delete --force "$SITE"

cp --force --recursive "$BASE/target/site/" "$SITE"

