#!/usr/bin/env bash
set -e

. $(dirname $0)/../settings.sh

echo "Executing 'sbt assemblyPackageDependency'..."
cd $SOURCE_DIR
sbt assemblyPackageDependency
cd -

echo "Moving the jar file to $APP_DIR..."
mkdir -p $APP_DIR
mv $SOURCE_DIR/target/scala-2.11/languagedetector-assembly-1.0-deps.jar $APP_DIR

