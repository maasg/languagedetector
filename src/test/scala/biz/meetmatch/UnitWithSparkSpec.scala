package biz.meetmatch

import biz.meetmatch.util.Utils
import org.apache.spark.sql.SparkSession

abstract class UnitWithSparkSpec extends UnitSpec {
  protected[this] implicit var sparkSession: SparkSession = _

  override def beforeAll() {
    sparkSession = Utils.createSparkSession()
  }

  override def afterAll() {
    sparkSession.sparkContext.stop
  }
}