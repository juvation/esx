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

if test ! -f build.xml
then
	echo ideally this script would be in the same directory as build.xml from the distro
	exit 1
fi

rm -f electribesx.jar

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
zip -r electribesx.zip build.sh print.sh wavexport.sh copy.sh

# ESX files
zip -r electribesx.zip factory.esx empty.esx

# 909 samples just because
zip -r electribesx.zip samples/TR909

# driver file for 909
zip -r electribesx.zip tr909.properties

# readme file
zip -r electribesx.zip readme.txt

echo make-distro.command: done


