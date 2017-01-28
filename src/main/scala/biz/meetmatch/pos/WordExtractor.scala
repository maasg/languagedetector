package biz.meetmatch.pos

import java.util.Properties

import biz.meetmatch.logging.BusinessLogger
import edu.stanford.nlp.ling.CoreAnnotations.{LemmaAnnotation, PartOfSpeechAnnotation, SentencesAnnotation, TokensAnnotation}
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.util.Try

class WordExtractor {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val businessLogger = BusinessLogger.forModule(this.getClass)

  private val props = new Properties()
  props.put("annotators", "tokenize, ssplit, pos, lemma")

  private val pipeline = Try(new StanfordCoreNLP(props))
    .getOrElse {
      logger.warn("failed to create pipeline, retrying once...")
      businessLogger.warn("StanfordNLPCore", "failed to create pipeline, retrying once...")
      new StanfordCoreNLP(props)
    }

  private lazy val languageDetector = new LanguageDetector

  def convertTextToValidLemmas(text: String, groupWords: Boolean = false, wordGroupSuggestions: Set[String] = Set.empty, removeForeignText: Boolean = false): Array[Array[String]] = {
    convertTextToValidTags(text, groupWords, wordGroupSuggestions, removeForeignText).map(_.map(_.lemma))
  }

  /**
    *
    * @param text                 should be case sensitive if proper nouns are not accepted (will also return the results with the casing unmodified)
    * @param wordGroupSuggestions should be lower case
    */
  def convertTextToValidTags(text: String, groupWords: Boolean = false, wordGroupSuggestions: Set[String] = Set.empty, removeForeignText: Boolean = false): Array[Array[Tag]] = {
    convertTextToTags(text)
  }

  def convertTextToTags(text: String): Array[Array[Tag]] = {
    val tokens = convertTextToTokens(text)

    val tags = tokens
      .map { sentence =>
        sentence.map { token =>
          val tag = token.get(classOf[PartOfSpeechAnnotation])
          val lemma = token.get(classOf[LemmaAnnotation])

          Tag(token.originalText, tag, lemma)
        }
      }

    tags.filter(_.nonEmpty)
  }

  def convertTextToTokens(text: String): Array[Array[CoreLabel]] = {
    val doc = new Annotation(text)
    pipeline.annotate(doc)

    doc.get(classOf[SentencesAnnotation]).asScala
      .map { sentence =>
        sentence
          .get(classOf[TokensAnnotation])
          .asScala
          .toArray
      }
      .toArray
  }
}


