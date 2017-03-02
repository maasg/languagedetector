package biz.meetmatch.modules

import biz.meetmatch.model.{Sentence, WrongDetectionByLanguage}
import org.apache.spark.sql.{Dataset, SparkSession}
import org.rogach.scallop.Scallop

object CountWrongDetectionsByLanguage extends Module with ParquetExtensions[WrongDetectionByLanguage] {
  override val parquetFile = "WrongDetectionsByLanguage"

  override def execute(scallopts: Scallop)(implicit sparkSession: SparkSession): Unit = {
    val sentenceDS = DetectLanguage.loadResultsFromParquet

    val wrongDetectionByLanguageDS = calc(sentenceDS)

    saveResultsToParquet(wrongDetectionByLanguageDS)
  }

  /**
    *
    * @param sentenceDS   text
    * @param sparkSession spark session
    * @return
    */
  def calc(sentenceDS: Dataset[Sentence])(implicit sparkSession: SparkSession): Dataset[WrongDetectionByLanguage] = {
    import sparkSession.implicits._
    sparkSession.sparkContext.setJobGroup(this.getClass.getName, this.getClass.getName)

    sparkSession.sparkContext.setJobDescription("Count the wrong detections by language")

    // TASK 3: count how many wrongly detected sentences exist for each detected language and save the results in the WrongDetectionByLanguage case class
    // use the Dataset api

    ???
  }

  def loadResultsFromParquet(implicit module: Class[_] = this.getClass, sparkSession: SparkSession): Dataset[WrongDetectionByLanguage] = {
    import sparkSession.implicits._
    loadResultsFromParquetAsDF(module, sparkSession).as[WrongDetectionByLanguage]
  }
}

