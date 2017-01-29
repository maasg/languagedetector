#!/usr/bin/env bash

. $(dirname $0)/env.sh

echo "Installing java..."
yum install -y java
echo "Done."

echo "Installing spark..."
SPARK_VERSION=2.1.0
wget -P /tmp http://apache.belnet.be/spark/spark-$SPARK_VERSION/spark-$SPARK_VERSION-bin-hadoop2.7.tgz
mkdir -p $SPARK_HOME

tar xvf /tmp/spark-$SPARK_VERSION-bin-hadoop2.7.tgz -C /opt
#cp /opt/spark/conf/spark-env.sh /opt/spark-$SPARK_VERSION-bin-hadoop2.7/conf
ln -fs /opt/spark-$SPARK_VERSION-bin-hadoop2.7 $SPARK_HOME
mkdir -p /tmp/spark-events
echo "Done."

echo "Installing sbt..."
curl https://bintray.com/sbt/rpm/rpm | sudo tee /etc/yum.repos.d/bintray-sbt-rpm.repo
yum install -y sbt
echo "Done."

echo "Installing zeppelin..."
ZEPPELIN_VERSION=0.6.2
/opt/zeppelin/bin/zeppelin-daemon.sh stop
# don't install the version with all interpreters included because it fails to download the jars into spark (error: netty no such method)
wget -P /tmp http://apache.belnet.be/zeppelin/zeppelin-$ZEPPELIN_VERSION/zeppelin-$ZEPPELIN_VERSION-bin-netinst.tgz
mkdir -p /tmp/zeppelin_upgrade
cp -R /opt/zeppelin/conf /tmp/zeppelin_upgrade
cp -R /opt/zeppelin/notebook /tmp/zeppelin_upgrade
tar xvf /tmp/zeppelin-$ZEPPELIN_VERSION-bin-netinst.tgz -C /opt
ln -sf /opt/zeppelin-$ZEPPELIN_VERSION-bin-netinst /opt/zeppelin
mv /tmp/zeppelin_upgrade/conf /opt/zeppelin
mv /tmp/zeppelin_upgrade/notebook /opt/zeppelin
/opt/zeppelin/bin/zeppelin-daemon.sh start
echo "Done."

echo "Set the following env vars in /opt/zeppelin/conf/zeppelin-env.sh: SPARK_HOME, SPARK_SUBMIT_OPTIONS"
# export SPARK_HOME="/opt/spark"
# export SPARK_SUBMIT_OPTIONS="--driver-memory=32g --driver-java-options \"-Dconfig.file=/root/cruncher/test.conf\""
