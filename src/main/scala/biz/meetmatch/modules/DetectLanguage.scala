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
  def calc(textDS: Dataset[String], sample: Boolean = true)(implicit sparkSession: SparkSession): Dataset[Sentence] = {
    import sparkSession.implicits._
    sparkSession.sparkContext.setJobGroup(this.getClass.getName, this.getClass.getName)

    sparkSession.sparkContext.setJobDescription("Detect the language of the text")
    (if(sample)textDS.sample(withReplacement = true, 0.0005) else textDS)
      .map(line => line.split("\t"))
      .mapPartitions { sentences =>
        sentences.map { case Array(id, language, sentence) =>
          val languageDetector = new LanguageDetector()
          Sentence(sentence, language, languageDetector.detectLanguage(sentence))
        }
      }
  }

  def loadInputTextFromFile(path: String)(implicit sparkSession: SparkSession): Dataset[String] = {
    Utils.loadTextFileAbs(path)
  }

  def loadResultsFromParquet(implicit module: Class[_] = this.getClass, sparkSession: SparkSession): Dataset[Sentence] = {
    import sparkSession.implicits._
    loadResultsFromParquetAsDF(module, sparkSession).as[Sentence]
  }
}

