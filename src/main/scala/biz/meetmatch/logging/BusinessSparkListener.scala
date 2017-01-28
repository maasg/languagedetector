package biz.meetmatch.logging

import org.apache.spark.scheduler._

import scala.collection.mutable

class BusinessSparkListener extends SparkListener {
  private val jobGroups = mutable.HashMap[Long, String]()

  override def onJobStart(jobStart: SparkListenerJobStart): Unit = {
    val jobGroup = Option(jobStart.properties.getProperty("spark.jobGroup.id")).getOrElse("undefined")
    val jobDescription = Option(jobStart.properties.getProperty("spark.job.description")).getOrElse("undefined")
    val executionId = jobStart.properties.getProperty("spark.sql.execution.id")

    new BusinessLogger(jobGroup).jobStarted(jobStart.jobId, jobDescription, jobStart.stageInfos.size, Option(executionId))

    synchronized {
      jobGroups(jobStart.jobId) = jobGroup
    }
  }

  override def onJobEnd(jobEnd: SparkListenerJobEnd): Unit = {
    val result = jobEnd.jobResult match {
      case JobSucceeded => "SUCCESS"
      case _ => "FAILURE"
    }
    jobGroups.get(jobEnd.jobId).foreach(jobGroup => new BusinessLogger(jobGroup).jobStopped(jobEnd.jobId, result))
  }

//  override def onStageSubmitted(stageSubmitted: SparkListenerStageSubmitted): Unit = {
//    logger.info(s"Stage ${stageSubmitted.stageInfo.stageId} ${stageSubmitted.stageInfo.name} submitted with ${stageSubmitted.stageInfo.numTasks} tasks.")
//  }
//
//  override def onStageCompleted(stageCompleted: SparkListenerStageCompleted): Unit = {
//    logger.info(s"Stage ${stageCompleted.stageInfo.stageId} ${stageCompleted.stageInfo.name} completed.")
//  }

//  override def onTaskStart(taskStart: SparkListenerTaskStart): Unit = { }
//
//  override def onTaskGettingResult(taskGettingResult: SparkListenerTaskGettingResult): Unit = { }
//
//  override def onTaskEnd(taskEnd: SparkListenerTaskEnd): Unit = { }


//  override def onEnvironmentUpdate(environmentUpdate: SparkListenerEnvironmentUpdate): Unit = {}
//
//  override def onBlockManagerAdded(blockManagerAdded: SparkListenerBlockManagerAdded): Unit = {}
//
//  override def onBlockManagerRemoved(
//    blockManagerRemoved: SparkListenerBlockManagerRemoved): Unit = {}

//  override def onUnpersistRDD(unpersistRDD: SparkListenerUnpersistRDD): Unit = {
//    logger.info(s"RDD ${unpersistRDD.rddId} unpersisted")
//  }

//  override def onApplicationStart(applicationStart: SparkListenerApplicationStart): Unit = {applicationStart.}
//
//  override def onApplicationEnd(applicationEnd: SparkListenerApplicationEnd): Unit = {}

//  override def onExecutorMetricsUpdate(executorMetricsUpdate: SparkListenerExecutorMetricsUpdate): Unit = {
//    logger.info(s"Executor ${executorMetricsUpdate.execId} metrics update: ${executorMetricsUpdate.accumUpdates.foreach(au => s"${au._1} ${au._2} ${au._3} ${au._4.foreach { ai => ai.name.get }} ")}")
//  }

//  override def onExecutorAdded(executorAdded: SparkListenerExecutorAdded): Unit = {}
//
//  override def onExecutorRemoved(executorRemoved: SparkListenerExecutorRemoved): Unit = {}
//
//  override def onBlockUpdated(blockUpdated: SparkListenerBlockUpdated): Unit = {}

//  override def onOtherEvent(event: SparkListenerEvent): Unit = event match {
//    case SparkListenerSQLExecutionStart(executionId, description, details, physicalPlanDescription, sparkPlanInfo, time) =>
//      logger.info(s"SparkListenerSQLExecutionStart for execution id $executionId and description '$description'")
//    case SparkListenerSQLExecutionEnd(executionId, time) =>
//      logger.info(s"SparkListenerSQLExecutionEnd for execution id $executionId")
//    case otherEvent =>
//      logger.info(otherEvent.getClass.getName)
//  }
}