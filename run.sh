#!/bin/bash
# spring boot run
# Revision:    2.0
# Date:        2021/03/25
# Author:      xuyan

. /etc/profile
work_path=$(dirname $(readlink -f $0))
cd $work_path

ALIAS="FtpNoStructureData-0.0.1-SNAPSHOT"
JAR="$ALIAS.jar"
JAR_START="nohup java -jar -d64 -Xms256m -Xmx1024m -Xmn256m -Xss256k -XX:SurvivorRatio=8 -XX:+UseConcMarkSweepGC $JAR --spring.profiles.active=prod >$ALIAS.log 2>$ALIAS.error &"
psid=0
GETCOLONYID=`uname -a|awk '{print $2}'`

function checkJarPid()
{
	PID=$(ps -ef | grep "$JAR" | grep -v grep | awk '{ print $2 }')
	if [ -n "$PID" ]; then
		psid=$PID
	else
		psid=0
	fi
}

function start()
{
   checkJarPid

   if [ $psid -ne 0 ]; then
	  echo "================================"
	  echo "warn:$GETCOLONYID $ALIAS already started! (pid=$psid)"
	  echo "================================"
   else
	  echo "Starting $GETCOLONYID $ALIAS ..."
	  eval $JAR_START
	  checkJarPid
	  if [ $psid -ne 0 ]; then
		 echo "(pid=$psid) [OK]"
		 # showLog
	  else
		 echo "[Failed]"
		 # showError
	  fi
   fi
}

function stop()
{
   checkJarPid

   if [ $psid -ne 0 ]; then
	  echo -ne "\rStopping $GETCOLONYID $ALIAS ...(pid=$psid)"
	  kill $psid
	  if [ $? -ne 0 ]; then
		 echo "[Failed]"
	  fi

	  checkJarPid
	  if [ $psid -ne 0 ]; then
		 stop
	  fi
   else
   	echo
	  echo "================================"
	  echo "warn:$GETCOLONYID $ALIAS is not running"
	  echo "================================"
   fi
}

function rebuild()
{
	if [ -e bak/$JAR ]; then
    stop
    rm -rf $JAR
    mv bak/$JAR ./
    start
    fi
}

function showLog()
{
  eval "tail -200f $ALIAS.log"
}

function showError()
{
  eval "tail -200f $ALIAS.error"
}

function status()
{
   checkJarPid
   if [ $psid -ne 0 ];  then
	  echo "$GETCOLONYID $ALIAS is running! (pid=$psid)"
   else
	  echo "$GETCOLONYID $ALIAS is not running"
   fi
}

function info()
{
   echo "System Information:"
   echo "****************************"
   echo `uname -a`
   echo "$JAR"
   echo "****************************"
}

case "$1" in
   'start')
	  start
	  ;;
   'stop')
	 stop
	 ;;
   'restart')
	 stop &&
	 start
	 ;;
   'rebuild')
		 rebuild
	 ;;
   'log')
	 showLog
	 ;;
   'error')
	 showError
	 ;;
	 'status')
		 status
	 ;;
   'info')
	 info
	 ;;
  *)
echo "Param: $0 {start|stop|restart|rebuild|log|error|status|info}"
exit 1
esac
exit 0
