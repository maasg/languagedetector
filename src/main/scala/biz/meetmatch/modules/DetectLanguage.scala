package biz.meetmatch.modules

import biz.meetmatch.model.Sentence
import biz.meetmatch.pos.{LanguageDetector, SentenceExtractor}
import biz.meetmatch.util.Utils
import org.apache.spark.sql.{Dataset, SparkSession}
import org.rogach.scallop.Scallop

object DetectLanguage extends Module with ParquetExtensions[Sentence] {
  override val parquetFile = "Sentences"

  override def execute(scallopts: Scallop)(implicit sparkSession: SparkSession): Unit = {
    val inputFile = scallopts.get[String]("file").getOrElse(Utils.getConfig("languagedetector.inputFile"))

    val textDS = loadInputTextFromFile(inputFile)

    val JobTitleWithSimilaritiesDS = calc(textDS)

    saveResultsToParquet(JobTitleWithSimilaritiesDS)
  }

  /**
    *
    * @param textDS       text
    * @param sparkSession spark session
    * @return
    */
  def calc(textDS: Dataset[(String, String)])(implicit sparkSession: SparkSession): Dataset[Sentence] = {
    import sparkSession.implicits._
    sparkSession.sparkContext.setJobGroup(this.getClass.getName, this.getClass.getName)

    sparkSession.sparkContext.setJobDescription("Detect the language of the text")
    textDS.mapPartitions { text =>
      val sentenceExtractor = new SentenceExtractor()
      val languageDetector = new LanguageDetector()
      text.flatMap { case (fileName, content) =>
        sentenceExtractor
          .convertTextToSentences(content)
          .map(sentence => Sentence(sentence, languageDetector.detectLanguage(sentence)))
      }
    }
  }

  def loadInputTextFromFile(path: String)(implicit sparkSession: SparkSession): Dataset[(String, String)] = {
    Utils.loadWholeTextFileAbs(path)
  }

  def loadResultsFromParquet(implicit module: Class[_] = this.getClass, sparkSession: SparkSession): Dataset[Sentence] = {
    import sparkSession.implicits._
    loadResultsFromParquetAsDF(module, sparkSession).as[Sentence]
  }
}

