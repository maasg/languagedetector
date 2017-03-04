package biz.meetmatch.modules

import biz.meetmatch.language.LanguageDetector
import biz.meetmatch.model.Sentence
import biz.meetmatch.util.Utils
import org.apache.spark.sql.{Dataset, SparkSession}
import org.rogach.scallop.Scallop

object DetectLanguage extends Module with ParquetExtensions[Sentence] {
  override val parquetFile = "Sentences"

  override def execute(scallopts: Scallop)(implicit sparkSession: SparkSession): Unit = {
    val inputFile = scallopts.get[String]("file").getOrElse(Utils.getConfig("languagedetector.inputFile"))

    val textDS = loadInputTextFromFile(inputFile)

    val sentenceDS = calc(textDS)

    saveResultsToParquet(sentenceDS)
  }

  /**
    *
    * @param textDS       text
    * @param sparkSession spark session
    * @return
    */
  def calc(textDS: Dataset[String])(implicit sparkSession: SparkSession): Dataset[Sentence] = {
    import sparkSession.implicits._
    sparkSession.sparkContext.setJobGroup(this.getClass.getName, this.getClass.getName)

    sparkSession.sparkContext.setJobDescription("Detect the language of the text")
    textDS
      .repartition(Utils.getDefaultNumPartitions)

      // TASK 1:
      //  - split up the lines of the textDS dataset into separate parts - see datasets/sentences.tsv for the formatting (it also contains the actual language of the text that we want to use later to verify the detected language)
      //  - use the LanguageDetector() to translate the sentence and store the results in the Sentence case class

      // when finished coding:
      // - package, deploy and submit the spark application and verify the results using spark shell or a notebook (see https://github.com/tolomaus/languagedetector section Quick start - usage)
      // - verify the logs of the executed module in the language detector UI

      ???
  }

  def loadInputTextFromFile(path: String)(implicit sparkSession: SparkSession): Dataset[String] = {
    Utils.loadTextFileAbs(path)
  }

  def loadResultsFromParquet(implicit module: Class[_] = this.getClass, sparkSession: SparkSession): Dataset[Sentence] = {
    import sparkSession.implicits._
    loadResultsFromParquetAsDF(module, sparkSession).as[Sentence]
  }
}

