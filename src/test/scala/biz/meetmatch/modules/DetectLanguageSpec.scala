package biz.meetmatch.modules

import biz.meetmatch.UnitWithSparkSpec

class DetectLanguageSpec extends UnitWithSparkSpec {
  it should "detect the language of the sentences" in {
    val sqlC = sparkSession
    import sqlC.implicits._

    val textDS = Seq(
      ("file.txt", "Dit is een Nederlandstalige tekst. And this is a text written in English. Par conte, ça c'est un texte ecrit en Français.")
    ).toDS

    val sentences = DetectLanguage.calc(textDS).collect

    sentences.length should be(3)
    sentences(0).language should be("nl")
    sentences(1).language should be("en")
    sentences(2).language should be("fr")
  }
}
