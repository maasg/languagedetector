package biz.meetmatch.model

case class WrongDetectionByLanguage(actualLanguage: String, detectedLanguage: String, count: Long)
