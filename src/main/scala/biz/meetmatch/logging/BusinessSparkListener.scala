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
}