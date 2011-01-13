#/bin/sh

PROJ="barchart-udt4"

BASE="$WORKSPACE/$PROJ"
SITE="$WORKSPACE/site/$PROJ"

echo "### BASE=$BASE"
echo "### SITE=$SITE"

echo "### svn delete"
svn delete --force "$SITE"

echo "### copy new"
cp --verbose --force --recursive "$BASE/target/site/"* "$SITE/"

echo "### svn propset html"
svn propset --recursive svn:mime-type text/html *.html "$SITE"

echo "### svn propset css"
svn propset --recursive svn:mime-type text/css *.css "$SITE"

