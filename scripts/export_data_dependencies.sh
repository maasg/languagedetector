#!/usr/bin/env bash
set -e

echo "biz.meetmatch.util.DataDependencyPrinter.saveModuleDependenciesToJson" | sbt -Dconfig.resource=test.conf console
