#!/usr/bin/env bash
set -e

ENV=test

. $(dirname $0)/../settings.sh

CLASS=$1
DATE=$(date +"%Y%m%d%H%M")
BUSLOGFILE=businessLog_${DATE}.log
shift;

APP_CONFIG=${APP_DIR}/conf/${ENV}.conf

touch ${LOG_DIR}/${BUSLOGFILE}
ln -sf ${LOG_DIR}/${BUSLOGFILE} ${LOG_DIR}/businessLog_latest.log

mkdir -p /tmp/spark-events

echo "Start an IntelliJ remote debugging session to port 5005"
# add the dependencies to the class path using "--driver-class-path" otherwise the guava lib v14.0 from spark is taken while languagedetect needs >= 18.0
${SPARK_HOME}/bin/spark-submit --jars ${APP_DIR}/languagedetector-assembly-1.0-deps.jar --class biz.meetmatch.$CLASS --driver-memory 4g --driver-java-options "-Dconfig.file=${APP_CONFIG} -DbusinessLogFileName=${LOG_DIR}/${BUSLOGFILE} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005" --driver-class-path "${APP_DIR}/languagedetector-assembly-1.0-deps.jar" ${APP_DIR}/languagedetector_2.11-1.0.jar "$@"
