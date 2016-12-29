#!/bin/sh
cd "$(dirname $0)" && \
java -jar "HTGT.jar" && \
exit $? || status=$?

echo; echo "Das Programm wurde mit einem Fehler beendet: $status"
read -r -p "Zum Beenden die RETURN-Taste drücken..." dummy; echo
