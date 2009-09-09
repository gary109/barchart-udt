#! /bin/bash

#java -Djava.library.path=./lib -cp ./target/classes:./target/test-classes net.sourceforge.udt.MainTest

export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:./lib

CP=./target/dependency/*:./target/classes:./target/test-classes
MAIN=net.sourceforge.udt.MainTestFinalize

# -verbose:jni 

java -ea -Xms16m -Xmx16m -cp "$CP" $MAIN 

