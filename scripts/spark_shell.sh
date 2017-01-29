#!/usr/bin/env bash
set -e

. $(dirname $0)/env.sh

$(dirname $0)/build_assembly.sh

ENV=test
APP_CONFIG=${APP_DIR}/conf/${ENV}.conf

${SPARK_HOME}/bin/spark-shell --driver-memory 4g --driver-java-options "-Dconfig.file=${APP_CONFIG}" --jars ${APP_DIR}/languagedetector_2.11-1.0.jar,${APP_DIR}/languagedetector-assembly-1.0-deps.jar