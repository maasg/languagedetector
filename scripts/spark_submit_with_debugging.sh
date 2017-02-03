#!/usr/bin/env bash
set -e

ENV=test

. $(dirname $0)/../settings.env

CLASS=$1
DATE=$(date +"%Y%m%d%H%M")
BUSLOGFILE=businessLog_${DATE}.log
shift;

APP_CONFIG=/root/cruncher/${ENV}.conf

touch ${LOG_DIR}/${BUSLOGFILE}
ln -sf ${LOG_DIR}/${BUSLOGFILE} ${LOG_DIR}/businessLog_latest.log

echo "Start an IntelliJ remote debugging session to port 5005"
${SPARK_HOME}/bin/spark-submit --jars ${APP_DIR}/languagedetector-assembly-1.0-deps.jar --class biz.meetmatch.$CLASS --driver-memory 4g --driver-java-options "-Dconfig.file=${APP_CONFIG} -DbusinessLogFileName=/tmp/${BUSLOGFILE} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005" ${APP_DIR}/languagedetector_2.11-1.0.jar "$@"
