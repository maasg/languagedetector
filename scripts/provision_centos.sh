#!/usr/bin/env bash

. $(dirname $0)/../env.sh

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
