#!/bin/bash

# sort out where we are
DIR=`dirname $0`

if [ -z "$DIR" ]
then
	DIR=.
else
	cd $DIR
fi

echo make-distro.command: checking environment

if test ! -f electribesx.jar
then
	echo ideally this script would be in the same directory as electribesx.jar
	exit 1
fi

rm -f electribesx.jar

# fresh build, James
ant clean build

if test ! -f electribesx.jar
then
	echo please to fix build problems before making distro
	exit 1
fi

rm electribesx.zip

# add the jar
zip -r electribesx.zip electribesx.jar

# driver scripts
zip -r electribesx.zip build.sh print.sh wavexport.sh

# ESX files
zip -r electribsx.zip factory.esx empty.esx

# readme file - maybe i should write this
zip -r electribesx.zip readme.txt

# should we include some samples etc?

echo make-distro.command: done


