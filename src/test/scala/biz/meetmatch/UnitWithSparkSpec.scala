package biz.meetmatch

import biz.meetmatch.util.Utils
import org.apache.spark.sql.SparkSession
import org.scalatest._

abstract class UnitWithSparkSpec extends UnitSpec with Matchers with OptionValues with Inside with Inspectors with BeforeAndAfterAll {
  protected[this] implicit var sparkSession: SparkSession = _

  override def beforeAll() {
    sparkSession = Utils.createSparkSession()
  }

  override def afterAll() {
    sparkSession.sparkContext.stop
  }
}