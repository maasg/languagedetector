#!/usr/bin/env bash
set -e

. $(dirname $0)/../settings.sh

ENV=$1

if [ -z "$ENV" ]; then
    echo "You didn't specify the environment to which to deploy."
    exit 1
fi

APP_VERSION=$2

if [ -z "$APP_VERSION" ]; then
    echo "You didn't specify the version number to be deployed."
    exit 1
fi

APP_ENV_DIR=$APP_DIR/$ENV
echo "Deploying $APP_NAME dependencies $APP_VERSION to $APP_ENV_DIR..."
mkdir -p ${APP_ENV_DIR}
mv $SOURCE_DIR/target/scala-2.11/$APP_NAME-assembly-${APP_VERSION}-deps.jar ${APP_ENV_DIR}
ln -sfn ${APP_ENV_DIR}/$APP_NAME-assembly-${APP_VERSION}-deps.jar ${APP_ENV_DIR}/$APP_NAME-deps.jar

# note: in a real world scenario the package would be downloaded from a corporate repository instead of taking it from a local path

echo "Done."

