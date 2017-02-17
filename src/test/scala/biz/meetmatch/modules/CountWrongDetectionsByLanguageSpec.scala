package biz.meetmatch.modules

import biz.meetmatch.UnitWithSparkSpec
import biz.meetmatch.model.Sentence

class CountWrongDetectionsByLanguageSpec extends UnitWithSparkSpec {
  it should "detect the language of the sentences" in {
    val sqlC = sparkSession
    import sqlC.implicits._

    val sentenceDS = Seq(
      Sentence("Dit is een Nederlandstalige tekst", actualLanguage = "nld", detectedLanguage = "nl"),
      Sentence("En dit ook", actualLanguage = "nld", detectedLanguage = "de"),
      Sentence("And this is a text written in English", actualLanguage = "eng", detectedLanguage = "en"),
      Sentence("Par conte, ça c'est un texte ecrit en Français", actualLanguage = "fra", detectedLanguage = "fr")
    ).toDS

    val wrongDetections = CountWrongDetectionsByLanguage.calc(sentenceDS).collect

    wrongDetections should have length 1
    wrongDetections.find(_.actualLanguage == "nl").map(_.count) should be(Some(1))
  }
}
