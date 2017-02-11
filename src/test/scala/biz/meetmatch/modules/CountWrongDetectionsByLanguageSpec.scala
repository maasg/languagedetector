package biz.meetmatch.modules

import biz.meetmatch.UnitWithSparkSpec
import biz.meetmatch.model.Sentence

class CountWrongDetectionsByLanguageSpec extends UnitWithSparkSpec {
  it should "detect the language of the sentences" in {
    val sqlC = sparkSession
    import sqlC.implicits._

    val sentenceDS = Seq(
      Sentence("Dit is een Nederlandstalige tekst", language = "nld", detectedLanguage = "nl"),
      Sentence("En dit ook", language = "nld", detectedLanguage = "de"),
      Sentence("And this is a text written in English", language = "eng", detectedLanguage = "en"),
      Sentence("Par conte, ça c'est un texte ecrit en Français", language = "fra", detectedLanguage = "fr")
    ).toDS

    val countryCodes = List(
      "Dutch\tNL\tNLD",
      "German\tDE\tDEU",
      "English\tEN\tENG",
      "French\tFR\tFRA"
    )

    val wrongDetections = CountWrongDetectionsByLanguage.calc(sentenceDS, countryCodes).collect

    wrongDetections should have length 1
    wrongDetections.find(_.language == "nl").map(_.count) should be(Some(1))
  }
}
