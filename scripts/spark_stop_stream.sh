#!/usr/bin/env bash
set -e

CLASS=$1
shift;

if [ -f "/tmp/spark_stream_$CLASS.pid" ]; then
    kill -SIGTERM `cat /tmp/spark_stream_$CLASS.pid`
    rm -f /tmp/spark_stream_$CLASS.pid
    echo "Done."
else
    echo "$CLASS is not running so nothing to do."
fi
