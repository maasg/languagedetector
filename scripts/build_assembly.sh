#!/usr/bin/env bash
set -e

. $(dirname $0)/env.sh

#if grep -Fxq "val build = \"need new build\"" $SOURCE_DIR/build.sbt
#then
#    cp $HOME_DIR/src/main/resources/log4j.properties /opt/spark/conf

    echo "Executing 'sbt package'..."
    cd $SOURCE_DIR
    sbt package
    cd -

#    sed -c -i "s/val build = \"need new build\"/val build = \"build done\"/g" $SOURCE_DIR/build.sbt # todo: find out why the -c flag doesnt work on osx

    echo "Moving the jar file to $APP_DIR..."
    mkdir -p $APP_DIR
    mv $SOURCE_DIR/target/scala-2.11/languagedetector_2.11-1.0.jar $APP_DIR
#fi

