# Language detector
This repo contains an Apache Spark application that can detect the language of a text, sentence by sentence. See also [its companion repo](https://github.com/tolomaus/languagedetector_ui.git) which contains a Play application that allows to detect the language of a text that is entered by the user.

## Quick start
### set up
1] install Apache Spark 2.1.0 from http://spark.apache.org/downloads.html (you can use a different version if you also modify the sparkVersion accordingly in the [build.sbt](https://github.com/tolomaus/languagedetector/tree/master/build.sbt)) 

2] clone this repo
```shell
git clone https://github.com/tolomaus/languagedetector.git
cd languagedetector
```

3] (optional) modify the environment variables from [settings.sh](https://github.com/tolomaus/languagedetector/tree/master/settings.sh) to your needs
```shell
nano settings.sh
```

4] copy (and if necessary modify) the environment specific test.conf file:
```shell
. settings.sh
mkdir -p ${APP_DIR}/test
cp src/main/resources/test.conf ${APP_DIR}/test/
nano ${APP_DIR}/test/test.conf
```

If you haven't changed the settings in the two previous steps all files (binaries, data and logs) will be created under ~/my_workspace and may be deleted when you're done. 

### usage

OK now that we have set it all up let's have a look at how we can detect the languages of a text file containing sentences in different languages. We are going to assume that we're in the test environment.

```shell
# package and deploy the spark app dependencies
scripts/package_app_dependencies.sh # only run this when the dependencies have changed
scripts/deploy_app_dependencies.sh test 1.0 # only run this when the dependencies have changed

# package and deploy the spark app
scripts/package_app.sh # the version is currently set to 1.0 in the build.sbt
scripts/deploy_app.sh test 1.0 # deploy version 1.0 to the test environment

# submit the spark app with the example dataset that is included in this repo in the test environment
scripts/spark_submit.sh test workflow.Workflow --file datasets/sentences.tsv # the spark app is executed in the background but the logs are shown using a tail in the foreground so you can ctrl+c at any time without killing the spark app

# view the results
scripts/spark_shell.sh test # this script uses the settings from ${APP_DIR}/conf/test.conf - note: ignore the java.io.FileNotFoundException
scala> DetectLanguage.loadResultsFromParquet.collect # will return an array of Sentence(content: String, language: String)
scala> CountSentencesByLanguage.loadResultsFromParquet.collect # will return an array of SentenceCountByLanguage(language: String, count: Long)
```

## Documentation
The work is not finished when your code runs correctly in the notebook. You still have to put it into your end user's hands before you can actually get any value from it.  
The language detector comes with a set of lightweight and opinionated design principles and libraries that may help you in pushing your idea into production. In the following section the core pieces are explained.

### modules and workflows
You can split up your data processing work over a number of modules:
```scala
object CountSentencesByLanguage extends Module with ParquetExtensions[SentenceCountByLanguage] {
  override val parquetFile = "SentenceCountsByLanguage"

  override def execute(scallopts: Scallop)(implicit sparkSession: SparkSession): Unit = {
    val sentenceDS = DetectLanguage.loadResultsFromParquet

    val sentenceCountByLanguageDS = calc(sentenceDS)

    saveResultsToParquet(sentenceCountByLanguageDS)
  }

  def calc(sentenceDS: Dataset[Sentence])(implicit sparkSession: SparkSession): Dataset[SentenceCountByLanguage] = {
    sentenceDS
      .map(...)
      ...
  }
}
```

These modules can be combined in a workflow:
```scala
object Workflow extends WorkflowBase {
  override def getModules: Array[Module] = {
    (Array()
      :+ DetectLanguage
      :+ CountSentencesByLanguage
      ...
      )
  }
}
```


#### functional programming stype
#### unit testing
#### parquet extensions
#### ops mindset

### packaging and environment-awareness
Scipts exist to package the application and its dependencies into jar files. These files can be passed on to Spark, together with the module or workflow you want it to execute. 
```bash
scripts/spark_submit.sh modules.DetectLanguage --file datasets/sentences.tsv # run one module
scripts/spark_submit.sh workflow.Workflow --file datasets/sentences.tsv # run a workflow consisting of one or more modules
```
Information that is specific to an environment like cpu settings, passwords, filesystem locations, etc is kept in environment-specific config files. When an application is executed, it is important to pass the config file that applies to the correct environment. The scripts have the environment 'test' hardcoded for now so they will use the ```${APP_DIR}/conf/test.conf``` file

### online platform
Most of the time you will want to make the results of the heavy data processing available to your end users. The language detector UI shows how you can do this. It consists of a Play/scala web application and an Angular.js front end. At the moment it will directly read the parquet files that were generated from Spark for each request, but a more scalable solution could be to either cache the parquet data in memory (if the dataset fits in memory) or to use an intermediary database (e.g. Cassandra if you have key-based data retrieval)

Interactive use of the model:
![alt text](https://github.com/tolomaus/languagedetector/blob/master/docs/language-detection.png "language-detection")

Analytics:
![alt text](https://github.com/tolomaus/languagedetector/blob/master/docs/language-detection-analytics.png "language-detection-analytics")

### application-focused logging
In addition to the more infrastructure-focused logging that is generated by Spark itself and that can be seen in the Spark UI there is also an application-focused logging system available that you can consult from the language detector UI, similar to this one:
![alt text](https://github.com/tolomaus/languagedetector/blob/master/docs/spark-logging.png "spark-logging")

A section is available for each of the executed modules that shows the parquet files or jdbc calls that served as the inputs, the parquet files that were produced, their sizes and diffs, any warnings or errors that may have been generated, etc. 

### data dependencies
Data dependencies between modules are derived automatically and can be consulted in the language detector UI:
![alt text](https://github.com/tolomaus/languagedetector/blob/master/docs/data-dependencies.png "data-dependencies")

Make sure to export the data dependencies first using ```scripts/export_data_dependencies.sh```

