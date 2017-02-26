#!/usr/bin/env bash
set -e

. $(dirname $0)/spark_submit_prep.sh "$@"
shift;
shift;

echo "Start a VisualVM JMX session to myservername.com:9080"
# add the dependencies to the class path using "--driver-class-path" otherwise the guava lib v14.0 from spark is taken while languagedetect needs >= 18.0
${SPARK_HOME}/bin/spark-submit --jars ${APP_DIR}/languagedetector-assembly-1.0-deps.jar --class biz.meetmatch.$CLASS --driver-memory 2g --driver-java-options "-Dconfig.file=${APP_CONFIG} -DbusinessLogFileName=${LOG_DIR}/${BUSLOGFILE} -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=8090 -Dcom.sun.management.jmxremote.rmi.port=8091 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=myservername.com" --driver-class-path "${APP_DIR}/languagedetector-assembly-1.0-deps.jar" ${APP_DIR}/languagedetector_2.11-1.0.jar "$@"
