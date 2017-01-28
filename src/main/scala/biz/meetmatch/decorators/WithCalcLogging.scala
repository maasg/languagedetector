package biz.meetmatch.decorators

import biz.meetmatch.logging.BusinessLogger
import org.apache.spark.sql.SparkSession
import org.rogach.scallop.Scallop

import scala.util.{Failure, Success, Try}

object WithCalcLogging {
  def apply[B, U](clas: Class[U])(f: => B): B = apply(clas, None, None)(f)

  def apply[B, U](clas: Class[U], scallopts: Scallop)(f: => B): B = apply(clas, Some(scallopts), None)(f)

  def apply[B, U](clas: Class[U], scallopts: Scallop, sparkSession: SparkSession)(f: => B): B = apply(clas, Some(scallopts), Some(sparkSession))(f)

  def apply[B, U](clas: Class[U], scalloptsO: Option[Scallop], sparkSessionO: Option[SparkSession])(f: => B): B = {
    val businessLogger = BusinessLogger.forModule(clas)

    val optsString = scalloptsO
      .map { scallopts =>
        scallopts.opts
          .map { opt => opt.name + "=" + scallopts(opt.name)(opt.converter.tag) }
          .mkString(",")
      }
      .getOrElse("")

    val sparkAppId = sparkSessionO.map(_.sparkContext.applicationId).getOrElse("")

    businessLogger.calcStarted(optsString, sparkAppId)
    val attempt = Try(WithStopwatch(f))

    attempt match {
      case Success(result) =>
        businessLogger.calcStopped("SUCCESS")
        result
      case Failure(exception) =>
        businessLogger.calcStopped("FAILURE")
        throw exception
    }
  }
}
