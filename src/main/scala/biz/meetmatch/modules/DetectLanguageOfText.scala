package biz.meetmatch.modules

import biz.meetmatch.decorators.{WithCalcLogging, WithSparkSession}
import biz.meetmatch.pos.LanguageDetector
import biz.meetmatch.util.Utils
import org.apache.spark.sql.{Dataset, SparkSession}
import org.slf4j.{Logger, LoggerFactory}

object DetectLanguageOfText extends Module with ParquetExtensions[Sentence] {
  override protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override val parquetFile = "Sentences"

  override def main(args: Array[String]): Unit = {
    WithCalcLogging(this.getClass) {
      WithSparkSession(this.getClass) { implicit sparkSession =>
        execute
      }
    }
  }

  override def execute(implicit sparkSession: SparkSession): Unit = {
    val textDS = loadInputTextFromFile("???")

    val JobTitleWithSimilaritiesDS = calc(textDS)

    saveResultsToParquet(this.getClass, JobTitleWithSimilaritiesDS)
  }

  /**
    *
    * @param textDS text
    * @param sparkSession spark session
    * @return
    */
  def calc(textDS: Dataset[String])(implicit sparkSession: SparkSession): Dataset[Sentence] = {
    import sparkSession.implicits._
    sparkSession.sparkContext.setJobGroup(this.getClass.getName, this.getClass.getName)

    sparkSession.sparkContext.setJobDescription("Detect the language of the text")
    textDS.mapPartitions { text =>
      val languageDetector = new LanguageDetector()
      text.map(line => Sentence(line, languageDetector.detectLanguage(line)))
    }
  }

  def loadInputTextFromFile(path: String)(implicit sparkSession: SparkSession): Dataset[String] = {
    Utils.loadTextFile(path)
  }

  def loadResultsFromParquet(implicit sparkSession: SparkSession): Dataset[Sentence] = {
    loadResultsFromParquet(this.getClass)
  }

  def loadResultsFromParquet[U](module: Class[U])(implicit sparkSession: SparkSession): Dataset[Sentence] = {
    import sparkSession.implicits._
    loadResultsFromParquetAsDF(module).as[Sentence]
  }
}

case class Sentence(content: String, language: String)
