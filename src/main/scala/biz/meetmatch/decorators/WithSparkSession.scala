package biz.meetmatch.decorators

import biz.meetmatch.util.Utils
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

object WithSparkSession {
  def apply[B, U](clas: Class[U], streaming: Boolean = false, sparkPort: Int = 4040)(f: SparkSession => B): B = {
    logger.info("Creating spark context...")
    val sparkSession = Utils.createSparkSession(clas.getSimpleName, streaming, sparkPort)
    val result = f(sparkSession)
    logger.info("Stopping spark context...")
    sparkSession.sparkContext.stop
    logger.info("Done.")

    result
  }

  private val logger = LoggerFactory.getLogger(this.getClass)
}
