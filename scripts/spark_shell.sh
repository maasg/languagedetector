#!/usr/bin/env bash
set -e

ENV=$1

if [ -z "$ENV" ]; then
    echo "You didn't specify the environment."
    exit 1
fi

. $(dirname $0)/../settings.sh

APP_ENV_DIR=${APP_DIR}/${ENV}
APP_CONFIG=${APP_ENV_DIR}/${ENV}.conf

LOG_DIR=${APP_ENV_DIR}/logs
mkdir -p ${LOG_DIR}
BUSLOGFILE=businessLog_spark_shell.log
touch ${LOG_DIR}/${BUSLOGFILE}

${SPARK_HOME}/bin/spark-shell --driver-memory 1g --driver-java-options "-Dconfig.file=${APP_CONFIG}" --jars ${APP_ENV_DIR}/${APP_NAME}.jar,${APP_ENV_DIR}/${APP_NAME}-deps.jar -i $(dirname $0)/spark_shell_init.scala