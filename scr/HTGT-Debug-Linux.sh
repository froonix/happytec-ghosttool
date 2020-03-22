#!/bin/sh
java=`which java`
if test -z "$java"
then
	echo "ERROR: Java not found!" 1>&2
	exit 1
fi

echo "executable: $java"
"$java" -version

cd "$(dirname $0)" && \
"$java" -jar "HTGT.jar" -d 2>&1 | tee -a HTGT-Debug.log && \
exit $? || status=$?

echo; echo "Das Programm wurde mit einem Fehler beendet: $status"
read -r -p "Zum Beenden die RETURN-Taste drücken..." dummy; echo
