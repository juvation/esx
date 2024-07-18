#!/bin/bash

# sort out where we are
DIR=`dirname $0`

if [ -z "$DIR" ]
then
	DIR=.
else
	cd $DIR
fi

echo update-site.command: checking environment

if test ! -f make-distro.command
then
	echo ideally this script would be in the same directory as make-distro.command
	exit 1
fi

PEM_PATH=$HOME/ec2/Redfish.pem

if test ! -f $PEM_PATH
then
	echo script requires access to EC2 PEM file to copy to electribesx.com
	exit 1
fi

rm -f electribesx.zip

./make-distro.command

if test ! -f electribesx.zip
then
	echo make-distro.command did not make electribesx.zip, please to rectify environment
	exit 1
fi

# copy the tool distro over
scp -i $PEM_PATH electribesx.zip ec2-user@electribesx.com:electribesx.com

# now copy the site stuffs
scp -i $PEM_PATH site/*.* ec2-user@electribesx.com:electribesx.com
# ssh -i $PEM_PATH ec2-user@electribesx.com mkdir -p electribesx.com/positron
# scp -i $PEM_PATH site/positron/*.* ec2-user@electribesx.com:electribesx.com/positron/


