# languagedetector
This language detector repo can detect the language of a text, sentence by sentence

## quick start
### installation and configuration
1. install Spark 2.1.0 (you can use a different version if you also modify the sparkVersion accordingly in the [build.sbt](https://github.com/tolomaus/languagedetector/tree/master/build.sbt)) from http://spark.apache.org/downloads.html
2. clone this repo
```shell
git clone https://github.com/tolomaus/languagedetector.git
```
3. (optional) modify the environment variables from [env.sh](https://github.com/tolomaus/languagedetector/tree/master/env.sh)
```shell
nano env.sh
```
4. copy (and if necessary modify) the environment specific test.conf file:
```shell
. env.sh
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
scala> Utils.loadParquetFile("Sentences").collect # will return the detected language of each sentence
```