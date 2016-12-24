#!/bin/sh
cd "$(dirname $0)" && \
java -jar "HTGT.jar" && \
exit $?
