#/bin/sh

PROJ="barchart-udt4"

BASE="$WORKSPACE/$PROJ"
SITE="$WORKSPACE/site/$PROJ"

echo "### BASE=$BASE"
echo "### SITE=$SITE"

echo "### svn delete"
svn delete --force "$SITE"

echo "### svn commit"
svn commit --message "clear site" "$SITE"

echo "### mkdir new"
mkdir --parents "$SITE"

echo "### copy new"
cp --verbose --force --recursive "$BASE/target/site/"* "$SITE/"

echo "### svn add new"
svn add --force "$SITE"

echo "### svn propset html"
svn propset --force --recursive svn:mime-type text/html "$SITE"/*.html

echo "### svn propset css"
svn propset --force --recursive svn:mime-type text/css "$SITE"/*.css

