#!/usr/bin/env bash
set -e

ENV=test

CLASS=$1
shift;

if [ -f "${LOG_DIR}/spark_stream_${CLASS}_${ENV}.pid" ]; then
    kill -SIGTERM `cat ${LOG_DIR}/spark_stream_${CLASS}_${ENV}.pid`
    rm -f ${LOG_DIR}/spark_stream_${CLASS}_${ENV}.pid
    echo "Done."
else
    echo "${CLASS} is not running so nothing to do."
fi
