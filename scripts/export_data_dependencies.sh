#!/usr/bin/env bash
set -e

echo "biz.meetmatch.util.DataDependencyPrinter.exportModuleDependenciesToJson" | sbt -Dconfig.resource=test.conf console
