#!/usr/bin/env bash
set -e

ENV=test

CLASS=$1
shift;

if [ -f "/tmp/spark_stream_${CLASS}_${ENV}.pid" ]; then
    kill -SIGTERM `cat /tmp/spark_stream_${CLASS}_${ENV}.pid`
    rm -f /tmp/spark_stream_${CLASS}_${ENV}.pid
    echo "Done."
else
    echo "${CLASS} is not running so nothing to do."
fi
