package biz.meetmatch.decorators

import biz.meetmatch.util.Utils
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

object WithSparkSession {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def apply[B](streaming: Boolean = false, sparkPort: Int = 4040)(f: SparkSession => B)(implicit module: Class[_]): B = {
    logger.info("Creating spark context...")
    val sparkSession = Utils.createSparkSession(module.getName, streaming, sparkPort)
    val result = f(sparkSession)
    logger.info("Stopping spark context...")
    sparkSession.sparkContext.stop
    logger.info("Done.")

    result
  }
}
