package biz.meetmatch.modules

import biz.meetmatch.UnitWithSparkSpec
import biz.meetmatch.model.Sentence

class CountSentencesByLanguageSpec extends UnitWithSparkSpec {
  it should "detect the language of the sentences" in {
    val sqlC = sparkSession
    import sqlC.implicits._

    val sentenceDS = Seq(
      Sentence("Dit is een Nederlandstalige tekst", detectedLanguage = "nl"),
      Sentence("En dit ook", detectedLanguage = "nl"),
      Sentence("And this is a text written in English", detectedLanguage = "en"),
      Sentence("Par conte, ça c'est un texte ecrit en Français", detectedLanguage = "fr")
    ).toDS

    val sentenceCounts = CountSentencesByLanguage.calc(sentenceDS).collect

    sentenceCounts should have length 3
    sentenceCounts.find(_.detectedLanguage == "nl").map(_.count) should be(Some(2))
    sentenceCounts.find(_.detectedLanguage == "en").map(_.count) should be(Some(1))
    sentenceCounts.find(_.detectedLanguage == "fr").map(_.count) should be(Some(1))
  }
}
