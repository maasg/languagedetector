#!/usr/bin/env bash
set -e

# we have to use this from spark because it needs the spark libraries to find it's stuff
$(dirname $0)/spark_submit.sh util.DataDependencyPrinter
echo "paste these above statements in http://console.neo4j.org/ to visualize the data dependencies between the modules"

