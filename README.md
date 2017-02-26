# Language detector
This repo contains an Apache Spark application that can detect the language of a text, sentence by sentence. See also [its companion repo](https://github.com/tolomaus/languagedetector_ui.git) which contains a Play application that allows to detect the language of a text that is entered by the user. 

The main purpose of this repo however, is to offer a foundation that contains all the necessary bits and pieces to put machine learning models such as this language detector into production usage. Concerns like logging, unit testing, debugging, environment segregation, packaging and deployment are of critical importance to the long term maintainability of your solution and really have to be taken into account from the early start.   

## Quick start
### set up
1] install Apache Spark 2.1.0 from http://spark.apache.org/downloads.html (you can use a different version if you also modify the sparkVersion accordingly in the [build.sbt](https://github.com/tolomaus/languagedetector/tree/master/build.sbt)). 

Windows is also supported but takes a little more effort to set up. See https://jaceklaskowski.gitbooks.io/mastering-apache-spark/content/spark-tips-and-tricks-running-spark-windows.html for more details.

2] clone this repo
```bash
git clone https://github.com/tolomaus/languagedetector.git
cd languagedetector
```

3] (optional) modify the environment variables from [settings.sh](https://github.com/tolomaus/languagedetector/tree/master/settings.sh) to your needs
```bash
nano settings.sh
```

4] copy (and if necessary modify) the environment specific test.conf file:
```bash
. settings.sh
mkdir -p ${APP_DIR}/test
cp src/main/resources/test.conf ${APP_DIR}/test/
nano ${APP_DIR}/test/test.conf
```

On Windows:
```script
call settings.bat
mkdir %APP_DIR%\test
copy src\main\resources\test.conf %APP_DIR%\test
start notepad "%APP_DIR%\test\test.conf"
```

If you haven't changed the settings in the two previous steps all files (binaries, data and logs) will be created under ~/my_workspace and can safely be deleted when you're done. 

### usage

OK now that we have set it all up let's have a look at how we can detect the languages of a text file containing sentences in different languages. We are going to assume that we're in the test environment.

```bash
# package and deploy the spark app dependencies
scripts/package_app_dependencies.sh # only run this when the dependencies have changed
scripts/deploy_app_dependencies.sh test 1.0 # only run this when the dependencies have changed

# package and deploy the spark app
scripts/package_app.sh # the version is currently set to 1.0 in the build.sbt
scripts/deploy_app.sh test 1.0 # deploy version 1.0 to the test environment

# submit the spark app with the example dataset that is included in this repo in the test environment and execute the whole workflow containing the three modules 
scripts/spark_submit.sh test workflow.Workflow --file datasets/sentences.tsv # the spark app is executed in the background but the logs are shown using a tail in the foreground so you can ctrl+c at any time without killing the spark app

# or submit the spark app with the example dataset that is included in this repo in the test environment end execute the modules one by one
scripts/spark_submit.sh test modules.DetectLanguage --file datasets/sentences.tsv
scripts/spark_submit.sh test modules.CountSentencesByLanguage
scripts/spark_submit.sh test modules.CountWrongDetectionsByLanguage

# view the results
scripts/spark_shell.sh test # this script uses the settings from ${APP_DIR}/conf/test.conf
scala> DetectLanguage.loadResultsFromParquet.collect # will return an array of Sentence(content: String, language: String)
scala> CountSentencesByLanguage.loadResultsFromParquet.collect # will return an array of SentenceCountByLanguage(language: String, count: Long)
scala> CountWrongDetectionsByLanguage.loadResultsFromParquet.collect # will return an array of WrongDetectionByLanguage(detectedLanguage: String, count: Long)
```

## Documentation
The work is not finished when your code runs correctly in the notebook. You still have to put it into your end user's hands before you can actually get any value from it.  

This repository comes with a set of opinionated design principles and a lightweight framework that may help you in pushing your own idea into production. In the following sections all of the core pieces are explained in detail.

### Modules and workflows
The data processing logic can be split up over a number of modules and workflows.

A module is an "atomic" piece of logic that is typically fed one or more input datasets and produces one output dataset. The output of one module then serves as the input of one or more other modules and as such form a data flow (see further on how this can automatically be derived and visualized). 

See here an example of a module:
```scala
object DetectLanguage extends Module {
  override def execute(scallopts: Scallop)(implicit sparkSession: SparkSession): Unit = {
    val inputFile = scallopts.get[String]("file").get
    val textDS = loadInputTextFromFile(inputFile)

    val sentenceDS = calc(textDS)
      
    // now do something with the results...
  }

  def calc(textDS: Dataset[String])(implicit sparkSession: SparkSession): Dataset[Sentence] = {
    textDS
      .map(line => line.split("\t"))
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
      :+ CountWrongDetectionsByLanguage
      ...
      )
  }
}
```

##### parquet extensions

The datasets are stored in Parquet format, a columnar format which also contains the schema (metadata) of the data. In theory it is possible to keep all of the intermediary output datasets in memory and only store the final output datasets but usually you will also want to store the intermediary datasets for exploration or investigation. A module can be extended with ```ParquetExtensions``` to allow it to easily save its output dataset to parquet as well as to allow the downstream modules to easily load the dataset as its input.   

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

##### functional programming style
The logic with the side effects (mostly the loading of the input datasets and the storage of the output dataset) is pushed to the boundaries and kept in the ```execute``` method of the module. This method will then call the purely functional ```calc``` method to do the actual processing work. 

##### unit testing
Thanks to the separation between the side effects and the pure logic it becomes simple to unit test the logic. It suffices to create custom test data in each unit test, call the ```calc``` method and check the validity of the results

### Environment segregation
Information that is specific to an environment like cpu settings, passwords, filesystem locations, etc is kept in environment-specific config files. When an application is executed, it is important to pass the config file that applies to the correct environment.

### Packaging and deployment
Scipts exist to package the application and its dependencies into jar files and to deploy them to a specific environment. 
```bash
scripts/package_app.sh # the version is currently set to 1.0 in the build.sbt
scripts/deploy_app.sh test 1.0 # deploy version 1.0 to the test environment
```

The application can then be submitted to Spark by executing the ```spark_submit.sh``` script, passing the environment (as well as the module or workflow to run) as an input argument to the script
```bash
scripts/spark_submit.sh test modules.DetectLanguage --file datasets/sentences.tsv # run one module
scripts/spark_submit.sh test workflow.Workflow --file datasets/sentences.tsv # run a workflow consisting of one or more modules
```

### Remote debugging and monitoring
Scripts exist to easily start a remote debugging or VisualVM monitoring session.

##### debugging
```bash
scripts/spark_submit_with_debugging.sh test modules.DetectLanguage --file datasets/sentences.tsv
```

When using IntelliJ you can then connect to the remote session by running a configuration of type "Remote":

![alt text](https://github.com/tolomaus/languagedetector/blob/master/docs/debugging.png "debugging")

##### monitoring
```bash
scripts/spark_submit_with_monitoring.sh test modules.DetectLanguage --file datasets/sentences.tsv
```

When using VisualVM you can add a JMX connection to your_server:8090 while the spark application is running:

![alt text](https://github.com/tolomaus/languagedetector/blob/master/docs/monitoring.png "monitoring")

Note: make sure to modify the HOSTNAME in the ```spark_submit_with_monitoring.sh``` script to your needs

### Online platform
Most of the time you will want to make the results of the heavy data processing available to your end users. The language detector UI shows how you can do this. It consists of a Play/scala web application and an Angular.js front end. At the moment it will directly read the parquet files that were generated from Spark for each request, but a more scalable solution could be to either cache the parquet data in memory (if the dataset fits in memory) or to use an intermediary database (e.g. Cassandra if you have key-based data retrieval)

Interactive use of the model:

![alt text](https://github.com/tolomaus/languagedetector/blob/master/docs/language-detection.png "language-detection")

Analytics:

![alt text](https://github.com/tolomaus/languagedetector/blob/master/docs/language-detection-analytics.png "language-detection-analytics")

### Application-focused logging
In addition to the more infrastructure-focused logging that is generated by Spark itself and that can be seen in the Spark UI there is also an application-focused logging system available that you can consult from the language detector UI, similar to this one:

![alt text](https://github.com/tolomaus/languagedetector/blob/master/docs/spark-logging.png "spark-logging")

A section is available for each of the executed modules that shows the parquet files or jdbc calls that served as the inputs, the parquet files that were produced, their sizes and diffs, any warnings or errors that may have been generated, etc. 

### Data flow
The data flow between the modules is derived automatically and can be consulted in the language detector UI:

![alt text](https://github.com/tolomaus/languagedetector/blob/master/docs/data-flow.png "data-flow")

Make sure to export the data flow first by executing ```scripts/export_data_flow.sh```

