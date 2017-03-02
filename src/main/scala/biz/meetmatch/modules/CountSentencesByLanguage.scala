package biz.meetmatch.modules

import biz.meetmatch.model.{Sentence, SentenceCountByLanguage}
import org.apache.spark.sql.{Dataset, SparkSession}
import org.rogach.scallop.Scallop

object CountSentencesByLanguage extends Module with ParquetExtensions[SentenceCountByLanguage] {
  override val parquetFile = "SentenceCountsByLanguage"

  override def execute(scallopts: Scallop)(implicit sparkSession: SparkSession): Unit = {
    val sentenceDS = DetectLanguage.loadResultsFromParquet

    val sentenceCountByLanguageDS = calc(sentenceDS)

    saveResultsToParquet(sentenceCountByLanguageDS)
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

    // TASK 2: count how many sentences exist for each detected language and save the results in the SentenceCountByLanguage case class

    ???
  }

  def loadResultsFromParquet(implicit module: Class[_] = this.getClass, sparkSession: SparkSession): Dataset[SentenceCountByLanguage] = {
    import sparkSession.implicits._
    loadResultsFromParquetAsDF(module, sparkSession).as[SentenceCountByLanguage]
  }
}

