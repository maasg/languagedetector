#!/usr/bin/env bash
set -e

. $(dirname $0)/../settings.sh

ENV=test
APP_ENV_DIR=${APP_DIR}/${ENV}
APP_CONFIG=${APP_ENV_DIR}/${ENV}.conf

cd $SOURCE_DIR
sbt -Dconfig.file=${APP_CONFIG} "run-main biz.meetmatch.util.DataDependencyPrinter"
cd -
