package biz.meetmatch.modules

import biz.meetmatch.UnitWithSparkSpec
import biz.meetmatch.model.Sentence

class CountWrongDetectionsByLanguageSpec extends UnitWithSparkSpec {
  it should "count the wrong detections of the sentences by language" in {
    val sqlC = sparkSession
    import sqlC.implicits._

    val sentenceDS = Seq(
      Sentence("Dit is een Nederlandstalige tekst", actualLanguage = "nl", detectedLanguage = "nl"),
      Sentence("En dit ook", actualLanguage = "nl", detectedLanguage = "de"),
      Sentence("And this is a text written in English", actualLanguage = "en", detectedLanguage = "en"),
      Sentence("Par contre, ça c'est une texte ecrit en Français", actualLanguage = "fr", detectedLanguage = "fr")
    ).toDS

    val wrongDetections = CountWrongDetectionsByLanguage.calc(sentenceDS).collect

    wrongDetections should have length 1
    wrongDetections.find(_.detectedLanguage == "nl").map(_.count) should be(Some(1))
  }
}
