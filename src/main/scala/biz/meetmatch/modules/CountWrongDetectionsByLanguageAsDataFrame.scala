package biz.meetmatch.modules

import biz.meetmatch.model.{Sentence, WrongDetectionByLanguage}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.rogach.scallop.Scallop

object CountWrongDetectionsByLanguageAsDataFrame extends Module with ParquetExtensions[WrongDetectionByLanguage] {
  override val parquetFile = "WrongDetectionsByLanguage"

  override def execute(scallopts: Scallop)(implicit sparkSession: SparkSession): Unit = {
    val sentenceDS = DetectLanguage.loadResultsFromParquet

    val wrongDetectionByLanguageDS = calc(sentenceDS)

    saveResultsToParquetAsDF(wrongDetectionByLanguageDS)
  }

  /**
    *
    * @param sentenceDS   text
    * @param sparkSession spark session
    * @return
    */
  def calc(sentenceDS: Dataset[Sentence])(implicit sparkSession: SparkSession): DataFrame = {
    import sparkSession.implicits._
    sparkSession.sparkContext.setJobGroup(this.getClass.getName, this.getClass.getName)

    sparkSession.sparkContext.setJobDescription("Count the wrong detections by language")
    sentenceDS
      .filter($"detectedLanguage" =!= $"actualLanguage")
      .groupBy($"detectedLanguage")
      .count
      .select("detectedLanguage", "count")
  }

  def loadResultsFromParquet(implicit module: Class[_] = this.getClass, sparkSession: SparkSession): Dataset[WrongDetectionByLanguage] = {
    import sparkSession.implicits._
    loadResultsFromParquetAsDF(module, sparkSession).as[WrongDetectionByLanguage]
  }
}
