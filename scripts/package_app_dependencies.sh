#!/usr/bin/env bash
set -e

. $(dirname $0)/../settings.sh

echo "Creating package for dependencies..."
cd $SOURCE_DIR
sbt assemblyPackageDependency
cd -
echo "Done."

