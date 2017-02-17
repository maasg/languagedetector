#!/usr/bin/env bash
set -e

ENV=$1

if [ -z "$ENV" ]; then
    echo "You didn't specify the environment."
    exit 1
fi

. $(dirname $0)/../settings.sh

CLASS=$2

if [ -z "$CLASS" ]; then
    echo "You didn't specify the workflow or module to execute."
    exit 1
fi

DATE=$(date +"%Y%m%d%H%M")
LOGFILE=log_${DATE}.log
BUSLOGFILE=businessLog_${DATE}.log

APP_ENV_DIR=${APP_DIR}/${ENV}
APP_CONFIG=${APP_ENV_DIR}/${ENV}.conf

LOG_DIR=${APP_ENV_DIR}/logs
mkdir -p ${LOG_DIR}
touch ${LOG_DIR}/${LOGFILE}
ln -sf ${LOG_DIR}/${LOGFILE} ${LOG_DIR}/log_latest.log

touch ${LOG_DIR}/${BUSLOGFILE}
ln -sf ${LOG_DIR}/${BUSLOGFILE} ${LOG_DIR}/businessLog_latest.log

mkdir -p /tmp/spark-events
