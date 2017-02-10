#!/usr/bin/env bash
set -e

cd $(dirname $0)/..
sbt -Dconfig.resource=test.conf "run-main biz.meetmatch.util.DataDependencyPrinter"
cd -
