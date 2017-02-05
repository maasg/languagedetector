package biz.meetmatch.decorators

import biz.meetmatch.logging.BusinessLogger
import org.apache.spark.sql.SparkSession
import org.rogach.scallop.Scallop

import scala.util.{Failure, Success, Try}

object WithCalcLogging {
  def apply[B](f: => B)(implicit module: Class[_]): B = apply(module.getName)(f)

  def apply[B](scallopts: Scallop, sparkSession: SparkSession)(f: => B)(implicit module: Class[_] = this.getClass): B = apply(module.getName, Some(scallopts), Some(sparkSession))(f)

  def apply[B](module: String)(f: => B): B = apply(module, None, None)(f)

  def apply[B](module: String, scallopts: Scallop)(f: => B)(): B = apply(module, Some(scallopts), None)(f)

  def apply[B](module: String, scallopts: Scallop, sparkSession: SparkSession)(f: => B): B = apply(module, Some(scallopts), Some(sparkSession))(f)

  def apply[B](module: String, scalloptsO: Option[Scallop], sparkSessionO: Option[SparkSession])(f: => B): B = {
    val businessLogger = new BusinessLogger(module)

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
