#!/bin/sh
java=`which java`
if test -z "$java"
then
	echo "ERROR: Java not found!" 1>&2
	exit 1
fi

cd "$(dirname $0)" && \
"$java" -jar "HTGT.jar"
