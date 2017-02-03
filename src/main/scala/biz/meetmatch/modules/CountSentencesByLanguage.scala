package biz.meetmatch.modules

import biz.meetmatch.decorators.{WithCalcLogging, WithSparkSession}
import biz.meetmatch.model.{Sentence, SentenceCountByLanguage}
import biz.meetmatch.util.Utils
import org.apache.spark.sql.{Dataset, SparkSession}
import org.rogach.scallop.Scallop
import org.slf4j.{Logger, LoggerFactory}

object CountSentencesByLanguage extends Module with ParquetExtensions[SentenceCountByLanguage] {
  override protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override val parquetFile = "SentenceCountsByLanguage"

  override def main(args: Array[String]): Unit = {
    WithCalcLogging(this.getClass) {
      WithSparkSession(this.getClass) { implicit sparkSession =>
        execute(Utils.getFiltersFromCLI(args))
      }
    }
  }

  override def execute(scallopts: Scallop)(implicit sparkSession: SparkSession): Unit = {
    val sentenceDS = DetectLanguage.loadResultsFromParquet(this.getClass)

    val sentenceCountByLanguageDS = calc(sentenceDS)

    saveResultsToParquet(this.getClass, sentenceCountByLanguageDS)
  }

  /**
    *
    * @param sentenceDS   text
    * @param sparkSession spark session
    * @return
    */
  def calc(sentenceDS: Dataset[Sentence])(implicit sparkSession: SparkSession): Dataset[SentenceCountByLanguage] = {
    import sparkSession.implicits._
    sparkSession.sparkContext.setJobGroup(this.getClass.getName, this.getClass.getName)

    sparkSession.sparkContext.setJobDescription("Count the sentences by language")
    sentenceDS
      .groupByKey(_.language)
      .count
      .map { case (language, count) => SentenceCountByLanguage(language, count) }
  }

  def loadResultsFromParquet(implicit sparkSession: SparkSession): Dataset[SentenceCountByLanguage] = {
    loadResultsFromParquet(this.getClass)
  }

  def loadResultsFromParquet[U](module: Class[U])(implicit sparkSession: SparkSession): Dataset[SentenceCountByLanguage] = {
    import sparkSession.implicits._
    loadResultsFromParquetAsDF(module).as[SentenceCountByLanguage]
  }
}

