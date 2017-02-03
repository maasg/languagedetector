#!/usr/bin/env bash
set -e

ENV=test

. $(dirname $0)/../settings.sh

CLASS=$1
DATE=$(date +"%Y%m%d%H%M")
LOGFILE=log_${CLASS}_${DATE}.log
LOGFILELATEST=log_${CLASS}_latest.log
BUSLOGFILE=businessLog_dummy.log
shift;

APP_CONFIG=${APP_DIR}/conf/${ENV}.conf

touch ${LOG_DIR}/${LOGFILE}
ln -sf ${LOG_DIR}/${LOGFILE} ${LOG_DIR}/${LOGFILELATEST}

nohup ${SPARK_HOME}/bin/spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.1.0 --jars ${APP_DIR}/languagedetector-assembly-1.0-deps.jar --class biz.meetmatch.${CLASS} --driver-memory 8g --driver-java-options "-Dconfig.file=${APP_CONFIG} -DbusinessLogFileName=/tmp/${BUSLOGFILE}" ${APP_DIR}/languagedetector_2.11-1.0.jar "$@" > ${LOG_DIR}/${LOGFILE} 2>&1 &
echo $! > /tmp/spark_stream_${CLASS}_${ENV}.pid

tail -f ${LOG_DIR}/${LOGFILE}