#!/usr/bin/env bash
set -e

ENV=test

. $(dirname $0)/../env.sh

CLASS=$1
DATE=$(date +"%Y%m%d%H%M")
BUSLOGFILE=businessLog_${DATE}.log
shift;

APP_CONFIG=/root/cruncher/${ENV}.conf

touch ${LOG_DIR}/${BUSLOGFILE}
ln -sf ${LOG_DIR}/${BUSLOGFILE} ${LOG_DIR}/businessLog_latest.log

echo "Start a VisualVM JMX session to myservername.com:9080"
${SPARK_HOME}/bin/spark-submit --jars ${APP_DIR}/languagedetector-assembly-1.0-deps.jar --class biz.meetmatch.$CLASS --driver-memory 4g --driver-java-options "-Dconfig.file=${APP_CONFIG} -DbusinessLogFileName=/tmp/${BUSLOGFILE} -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=8090 -Dcom.sun.management.jmxremote.rmi.port=8091 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=myservername.com" ${APP_DIR}/languagedetector_2.11-1.0.jar "$@"
