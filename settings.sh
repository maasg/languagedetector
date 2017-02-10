#!/usr/bin/env bash
SOURCE_DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
APP_DIR=~/apps/languagedetector

LOG_DIR=~/logs
mkdir -p $LOG_DIR

SPARK_HOME=~/apps/spark-2.1.0-bin-hadoop2.7
