#!/bin/bash

echo "verstions:"

FILE="dump-with-optimizations.txt"

cat $FILE | grep 'GLIB' | \
sed 's/^.*\(GLIB[A-Z]*_[0-9\.]*\).*$/\1/' | \
sort -u
