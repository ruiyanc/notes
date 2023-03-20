#!/bin/bash
PID=$(ps -ef | grep FtpNoStructureData-0.0.1-SNAPSHOT.jar | grep -v grep | awk '{ print $2 }')
echo $PID
