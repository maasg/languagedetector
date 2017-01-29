package biz.meetmatch.modules

import java.sql.Timestamp

import biz.meetmatch.decorators.{WithCalcLogging, WithSparkSession}
import biz.meetmatch.logging.BusinessLogger
import biz.meetmatch.util.Utils
import org.apache.spark.sql.functions.max
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.slf4j.{Logger, LoggerFactory}

trait Module {
  protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    val scallopts = Utils.getFiltersFromCLI(args)
    logger.info(scallopts.summary)

    WithSparkSession(this.getClass) { implicit sparkSession =>
      WithCalcLogging(this.getClass, scallopts, sparkSession) {
        execute(args)
      }
    }
  }

  def execute(args: Array[String])(implicit sparkSession: SparkSession): Unit
}

abstract class StreamingModule(sparkPort: Int) {
  protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    val scallopts = Utils.getStreamingFiltersFromCLI(args)
    logger.info(scallopts.summary)

    WithSparkSession(this.getClass, streaming = true, sparkPort = sparkPort) { implicit sparkSession =>
      execute
    }
  }

  def execute(implicit sparkSession: SparkSession): Unit = {}
}

trait ParquetExtensions[T] {
  val parquetFile: String

  def saveResultsToParquet[U](module: Class[U], ds: Dataset[T])(implicit sparkSession: SparkSession): Unit = {
    val parquetFileTemp = parquetFile + ".inprogress"

    if (Utils.existsParquetFile(parquetFileTemp))
      Utils.deleteParquetFile(parquetFileTemp)

    import sparkSession.implicits._

      logger.debug(s"Saving $parquetFile to a temporary parquet file $parquetFileTemp...")
      try {
        Utils.saveAsParquetFile(ds, parquetFileTemp)
      } catch {
        case e: Exception =>
          logger.error(s"An error occurred while saving the temporary parquet file, leaving the original parquet file $parquetFile untouched.")
          throw e
      }

      sparkSession.sparkContext.setJobGroup(module.getClass.getName + ".save", module.getClass.getName + ".save")

      val countBefore =
        if (Utils.existsParquetFile(parquetFile)) {
          val parquetFileBefore = loadResultsFromParquetAsDF(module)
          sparkSession.sparkContext.setJobDescription(s"Count parquet file before - $parquetFile")
          parquetFileBefore.count
        }
        else
          0

      val parquetFileAfter = Utils.loadParquetFile(parquetFileTemp)
      sparkSession.sparkContext.setJobDescription(s"Count parquet file after - $parquetFile")
      val countAfter = parquetFileAfter.count
      BusinessLogger.forModule(module).dataParquetWritten(parquetFile, countBefore, countAfter)
      logger.info(s"Stored $countAfter $parquetFile, diff is ${countAfter - countBefore}")

      logger.debug(s"Moving the temporary parquet file $parquetFileTemp to its final destination...")
      if (Utils.existsParquetFile(parquetFile))
        Utils.deleteParquetFile(parquetFile)

      Utils.moveParquetFile(parquetFileTemp, parquetFile)
  }

  def loadResultsFromParquetAsDFO[U](implicit sparkSession: SparkSession): Option[DataFrame] = {
    if (Utils.existsParquetFile(parquetFile))
      Some(loadResultsFromParquetAsDF)
    else
      None
  }

  def loadResultsFromParquetAsDF[U](module: Class[U])(implicit sparkSession: SparkSession): DataFrame = {
    logger.info(s"Loading parquet from $parquetFile...")
    BusinessLogger.forModule(module).dataParquetRead(parquetFile)
    Utils.loadParquetFile(parquetFile) // dont call loadResultsFromParquetAsDF to avoid a stack overflow when it is overridden in the subclass
  }

  def loadResultsFromParquetAsDF(implicit sparkSession: SparkSession): DataFrame = {
    logger.info(s"Loading parquet from $parquetFile...")
    Utils.loadParquetFile(parquetFile)
  }

  def getLastModifiedFromParquetO(implicit sparkSession: SparkSession): Option[Timestamp] = {
    loadResultsFromParquetAsDFO
      .flatMap { dataframe =>
        if (dataframe.columns.contains("lastModified"))
          dataframe
            .select("lastModified")
            .agg(max("lastModified"))
            .collect
            .headOption
            .map(_.getTimestamp(0))
        else
          None
      }
  }

  private val logger = LoggerFactory.getLogger(this.getClass)
}
