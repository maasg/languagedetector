#!/usr/bin/env bash
set -e

. $(dirname $0)/../settings.sh

echo "Executing 'sbt assemblyPackageDependency'..."
cd $SOURCE_DIR
sbt assemblyPackageDependency
cd -
echo "Done."

