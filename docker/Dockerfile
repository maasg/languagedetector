FROM centos

RUN yum install -y git wget curl
ENV SPARK_VERSION 2.1.0
RUN wget -P /tmp http://apache.belnet.be/spark/spark-$SPARK_VERSION/spark-$SPARK_VERSION-bin-hadoop2.7.tgz && \
    tar xvf /tmp/spark-$SPARK_VERSION-bin-hadoop2.7.tgz -C /opt && \
    ln -s /opt/spark-$SPARK_VERSION-bin-hadoop2.7 /opt/spark && \
    mkdir -p /tmp/spark-eve
RUN curl https://bintray.com/sbt/rpm/rpm | tee /etc/yum.repos.d/bintray-sbt-rpm.repo && \
    yum install -y sbt
#RUN wget -P /tmp http://spark-notebook.io/dl/tgz/0.7.0/2.11/2.1.0/2.7.3/false/true && \
#    tar xvf /tmp/spark-notebook-0.7.0-scala-2.11.8-spark-2.1.0-hadoop-2.7.3.tgz -C /opt && \
#    ln -s /opt/spark-notebook-0.7.0-scala-2.11.8-spark-2.1.0-hadoop-2.7.3 /opt/spark-notebook
RUN mkdir src && cd src
RUN git clone https://github.com/tolomaus/languagedetector
RUN git clone https://github.com/tolomaus/languagedetector_ui

VOLUME /root

EXPOSE 9000
EXPOSE 4040
EXPOSE 18080
