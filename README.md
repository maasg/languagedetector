# languagedetector
This repo contains an Apache Spark application that can detect the language of a text, sentence by sentence. See also [its companion repo](https://github.com/tolomaus/languagedetector.git) which contains a Play application that allows to detect the language of a text that is entered by the user.

## quick start
### installation and configuration
1] install Apache Spark 2.1.0 from http://spark.apache.org/downloads.html (you can use a different version if you also modify the sparkVersion accordingly in the [build.sbt](https://github.com/tolomaus/languagedetector/tree/master/build.sbt)) 

2] clone this repo
```shell
git clone https://github.com/tolomaus/languagedetector.git
```

3] (optional) modify the environment variables from [settings.env](https://github.com/tolomaus/languagedetector/tree/master/settings.env) to your needs
```shell
nano settings.env
```

4] copy (and if necessary modify) the environment specific test.conf file to your needs:
```shell
. settings.env
cp src/main/resources/test.conf ${APP_DIR}/conf
nano ${APP_DIR}/conf/test.conf
```

### usage
```shell
# package the spark app
scripts/package_app_dependencies.sh # only run this when the dependencies have changed
scripts/package_app.sh

# submit the spark app
scripts/spark_submit.sh modules.DetectLanguage --file /path/to/file # the spark app is executed in the background but the logs are shown using a tail in the foreground so you can ctrl+c at any time without killing the spark app

# view the results
scripts/spark_shell.sh # note: ignore the java.io.FileNotFoundException
scala> DetectLanguage.loadResultsFromParquet.collect # will return an array of Sentence(content: String, language: String)
```
