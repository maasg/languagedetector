package biz.meetmatch.language

import biz.meetmatch.UnitSpec

class LanguageDetectorSpec extends UnitSpec {
  it should "detect the language of an English text" in {
    val extractor = new LanguageDetector()
    val text = "I like my job because it is the best in the world and there is nothing else."
    val expected = "en"

    extractor.detectLanguage(text) should be(expected)
  }
}
