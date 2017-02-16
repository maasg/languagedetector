#!/usr/bin/env bash
set -e

. $(dirname $0)/../settings.sh

ENV=test

cd $(dirname $0)/..
sbt -Dconfig.file=${APP_DIR}/conf/${ENV}.conf "run-main biz.meetmatch.util.DataDependencyPrinter"
cd -
