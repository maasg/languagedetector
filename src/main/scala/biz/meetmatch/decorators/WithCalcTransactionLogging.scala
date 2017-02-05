package biz.meetmatch.decorators

import biz.meetmatch.logging.BusinessLogger
import org.apache.spark.TaskContext

object WithCalcTransactionLogging {
  def apply[B, U](category: String, id: String, message: String = "")(f: => B)(implicit module: Class[_]): B = {
    val businessLogger = new BusinessLogger(module.getName)

    val taskContext = TaskContext.get
    businessLogger.transactionStarted(category, id, taskContext.stageId, taskContext.partitionId, taskContext.taskAttemptId, message)
    val result = f
    businessLogger.transactionStopped(category, id)

    result
  }
}
