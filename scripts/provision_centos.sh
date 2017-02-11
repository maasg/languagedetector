#!/usr/bin/env bash

. $(dirname $0)/../settings.sh

echo "Installing java..."
yum install -y java
echo "Done."

echo "Installing spark..."
SPARK_VERSION=2.1.0
wget -P /tmp http://apache.belnet.be/spark/spark-$SPARK_VERSION/spark-$SPARK_VERSION-bin-hadoop2.7.tgz
mkdir -p $SPARK_HOME

tar xvf /tmp/spark-$SPARK_VERSION-bin-hadoop2.7.tgz -C /opt
ln -fsn /opt/spark-$SPARK_VERSION-bin-hadoop2.7 $SPARK_HOME
mkdir -p /tmp/spark-events
echo "Done."

echo "Installing sbt..."
curl https://bintray.com/sbt/rpm/rpm | sudo tee /etc/yum.repos.d/bintray-sbt-rpm.repo
yum install -y sbt
echo "Done."

echo "Installing Spark Notebook..."
wget -P /tmp http://spark-notebook.io/dl/tgz/0.7.0/2.11/2.1.0/2.7.3/false/true
tar xvf /tmp/spark-notebook-0.7.0-scala-2.11.8-spark-2.1.0-hadoop-2.7.3.tgz
echo "Done."