package biz.meetmatch.modules

import biz.meetmatch.UnitWithSparkSpec

class DetectLanguageSpec extends UnitWithSparkSpec {
  it should "detect the language of the sentences" in {
    val sqlC = sparkSession
    import sqlC.implicits._

    val textDS = Seq(
      "nl\tDit is een Nederlandstalige tekst",
      "en\tAnd this is a text written in English",
      "fr\tPar contre, ça c'est une texte ecrit en Français"
    ).toDS

    val sentences = DetectLanguage.calc(textDS).collect

    sentences should have length 3
    sentences.foreach(sentence => sentence.detectedLanguage should be(sentence.actualLanguage))
  }
}
