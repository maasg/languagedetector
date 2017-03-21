#!/usr/bin/env bash
set -e

. $(dirname $0)/../settings.sh

echo "Creating package..."
cd $SOURCE_DIR
sbt package
cd -
echo "Done."

# note: in a real world scenario the package would be uploaded to a corporate repository

