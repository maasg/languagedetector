#!/usr/bin/env bash
set -e

. $(dirname $0)/spark_submit_prep.sh "$@"
shift;
shift;

HOSTNAME="localhost" // set this to the name of the server on which the spark app runs

echo "Start a VisualVM JMX session to ${HOSTNAME}:8090"
# add the dependencies to the class path using "--driver-class-path" otherwise the guava lib v14.0 from spark is taken while languagedetect needs >= 18.0
${SPARK_HOME}/bin/spark-submit --jars ${APP_ENV_DIR}/${APP_NAME}.jar --class biz.meetmatch.$CLASS --driver-memory 2g --driver-java-options "-Dconfig.file=${APP_CONFIG} -DbusinessLogFileName=${LOG_DIR}/${BUSLOGFILE} -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=8090 -Dcom.sun.management.jmxremote.rmi.port=8091 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=${HOSTNAME}" --driver-class-path "${APP_ENV_DIR}/${APP_NAME}-deps.jar" ${APP_ENV_DIR}/${APP_NAME}.jar "$@"
