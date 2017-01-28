package biz.meetmatch.logging

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import org.apache.spark.sql.SparkSession
import org.rogach.scallop.Scallop
import org.slf4j.LoggerFactory

object BusinessLogger {
  def forModule[U](module: Class[U]) = new BusinessLogger(module.getName)

  def getDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
}

class BusinessLogger[U](module: String) {
  private val logger = LoggerFactory.getLogger("businessLogger")

  def calcStarted(options: String, sparkAppId: String): Unit = {
    log(s"CALC\tSTART\t$options\t$sparkAppId")
  }

  def calcStopped(result: String): Unit = {
    log(s"CALC\tSTOP\t$result")
  }

  def jobStarted(jobId: Int, jobDescription: String, stageCount: Int, executionId: Option[String]): Unit = {
    log(s"JOB\t$jobId\tSTART\t${jobDescription.replace("\n", " ").replace("\t", " ")}\t$stageCount\t${executionId.getOrElse("")}")
  }

  def jobStopped(jobId: Int, result: String): Unit = {
    log(s"JOB\t$jobId\tSTOP\t$result")
  }

  def transactionStarted(category: String, id: String, stageId: Int = -1, partitionId: Int = -1, taskId: Long = -1, message: String = ""): Unit = {
    log(s"TRANSACTION\t$category\t$id\tSTART\t$stageId\t$partitionId\t$taskId\t${message.replace("\n", " ").replace("\t", " ")}")
  }

  def transactionStopped(category: String, id: String): Unit = {
    log(s"TRANSACTION\t$category\t$id\tSTOP")
  }

  def dataParquetRead(tableName: String, count: Long = -1): Unit = {
    log(s"DATA\tPARQUET\tREAD\t${tableName.replace("\n", " ").replace("\t", " ")}\t$count")
  }

  def dataParquetWritten(tableName: String, countBefore: Long, countAfter: Long): Unit = {
    log(s"DATA\tPARQUET\tWRITE\t${tableName.replace("\n", " ").replace("\t", " ")}\t$countBefore\t$countAfter")
  }

  def dataJdbcRead(tableName: String, count: Long = -1): Unit = {
    log(s"DATA\tJDBC\tREAD\t${tableName.replace("\n", " ").replace("\t", " ")}\t$count")
  }

  def dataJdbcWritten(tableName: String, countBefore: Long = -1, countAfter: Long = -1, countUpdated: Long = -1): Unit = {
    log(s"DATA\tJDBC\tWRITE\t${tableName.replace("\n", " ").replace("\t", " ")}\t$countBefore\t$countAfter\t$countUpdated")
  }

  def info(subject: String, message: String): Unit = {
    log(s"MESSAGE\tINFO\t${subject.replace("\n", " ").replace("\t", " ")}\t${message.replace("\n", " ").replace("\t", " ")}")
  }

  def warn(subject: String, message: String): Unit = {
    log(s"MESSAGE\tWARN\t${subject.replace("\n", " ").replace("\t", " ")}\t${message.replace("\n", " ").replace("\t", " ")}")
  }

  def error(subject: String, message: String): Unit = {
    log(s"MESSAGE\tERROR\t${subject.replace("\n", " ").replace("\t", " ")}\t${message.replace("\n", " ").replace("\t", " ")}")
  }

  private def log(line: String) = {
    logger.info(s"${BusinessLogger.getDateFormat.format(Calendar.getInstance.getTime)}\t$module\t$line")
  }
}

case class LogLineWorkflow(message: String, startDate: String, stopDate: Date, duration: String, state: String, options: Array[Array[String]], sparkAppId: String, calcs: Array[LogLineCalc], warnings: Int, errors: Int)

case class LogLineCalc(module: String, startDate: String, stopDate: Date, duration: String, state: String, options: Array[Array[String]], sparkAppId: String, jobs: Array[LogLineJob], transactionCategories: Array[LogLineTransactionCategory], dataReads: Array[LogLineDataRead], dataWrites: Array[LogLineDataWrite], messages: Array[LogLineMessage])
case class LogLineJob(id: Int, startDate: String, duration: String, state: String, description: String, stageCount: Int, executionId: Int = -1)

case class LogLineTransactionCategory(category: String, transactions: Array[LogLineTransaction], numberOfTransactions: Int, averageFinishedTransactionDuration: String)
case class LogLineTransaction(category: String, id: String, stageId: Int, partitionId: Int, taskId: Long, message: String, startDate: String, duration: String, state: String)

case class LogLineDataRead(storage: String, tableName: String, count: Int, date: String)

case class LogLineDataWrite(storage: String, tableName: String, countBefore: Int, countAfter: Int, countUpdated: Int, date: String)

case class LogLineMessage(category: String, subject: String, message: String, date: String)

