package biz.meetmatch.workflow

import biz.meetmatch.decorators.{WithCalcLogging, WithSparkSession}
import biz.meetmatch.modules.Module
import biz.meetmatch.util.Utils
import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}

trait WorkflowBase {
  protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    val scallopts = Utils.getFiltersFromCLI(args)
    logger.info(scallopts.summary)

    WithSparkSession(this.getClass) { implicit sparkSession =>
      WithCalcLogging(this.getClass, scallopts, sparkSession) {
        getModules.foreach(executeModule(_, args))
      }
    }
  }

  def executeModule(module: Module, args: Array[String])(implicit sparkSession: SparkSession): Unit = {
    logger.info("====================================================")
    logger.info("MODULE " + module.getClass.getSimpleName)
    logger.info("====================================================")
    WithCalcLogging(module.getClass) {
      sparkSession.sparkContext.setJobGroup(module.getClass.getName, this.getClass.getName)
      module match {
        case mod: Module => module.execute(args)
      }
      sparkSession.sparkContext.clearJobGroup
    }
    logger.info("====================================================")
    logger.info("")
  }

  def getModules: Array[Module]
}
