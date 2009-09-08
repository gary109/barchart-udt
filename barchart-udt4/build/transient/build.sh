#! /bin/bash

echo "sh-start"

echo "working directory: `pwd`"

make info
make all

# ls -las ./target/classes/*.so

echo "sh-finish"
