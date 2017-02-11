package biz.meetmatch.modules

import biz.meetmatch.model.{Sentence, WrongDetectionByLanguage}
import biz.meetmatch.util.Utils
import org.apache.spark.sql.{Dataset, SparkSession}
import org.rogach.scallop.Scallop

object CountWrongDetectionsByLanguage extends Module with ParquetExtensions[WrongDetectionByLanguage] {
  override val parquetFile = "WrongDetectionsByLanguage"

  override def execute(scallopts: Scallop)(implicit sparkSession: SparkSession): Unit = {
    val sentenceDS = DetectLanguage.loadResultsFromParquet
    val countryCodeTsv = Utils.loadWordsFromResource("countrycodes.tsv")

    val wrongDetectionByLanguageDS = calc(sentenceDS, countryCodeTsv)

    saveResultsToParquet(wrongDetectionByLanguageDS)
  }

  /**
    *
    * @param sentenceDS   text
    * @param sparkSession spark session
    * @return
    */
  def calc(sentenceDS: Dataset[Sentence], countryCodeTsv: List[String])(implicit sparkSession: SparkSession): Dataset[WrongDetectionByLanguage] = {
    import sparkSession.implicits._
    sparkSession.sparkContext.setJobGroup(this.getClass.getName, this.getClass.getName)

    val countryCodes = countryCodeTsv
      .tail // remove the header
      .map { line =>
          val Array(name, iso2, iso3) = line.toLowerCase.split("\t")
          (iso2, iso3)
      }
    val countryCodesBC = sparkSession.sparkContext.broadcast(countryCodes)

    sparkSession.sparkContext.setJobDescription("Count the wrong detections by language")
    sentenceDS
      .flatMap { sentence =>
        // only keep the sentences where a mapping was found between the two-character detected language and the three-character source language
        if(countryCodesBC.value.exists { case (iso2, iso3) => sentence.detectedLanguage == iso2 }){
          countryCodesBC.value
            .find { case (iso2, iso3) => sentence.language == iso3 }
            .map { case (iso2, iso3) => sentence.copy(language = iso2)}
        } else
          None
      }
      .filter(sentence => sentence.detectedLanguage != sentence.language)
      .groupByKey(sentence => (sentence.language, sentence.detectedLanguage))
      .count
      .map { case ((language, detectedLanguage), count) => WrongDetectionByLanguage(language, detectedLanguage, count) }
  }

  def loadResultsFromParquet(implicit module: Class[_] = this.getClass, sparkSession: SparkSession): Dataset[WrongDetectionByLanguage] = {
    import sparkSession.implicits._
    loadResultsFromParquetAsDF(module, sparkSession).as[WrongDetectionByLanguage]
  }
}

