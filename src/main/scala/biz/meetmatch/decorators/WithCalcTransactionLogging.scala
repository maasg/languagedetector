package biz.meetmatch.decorators

import biz.meetmatch.logging.BusinessLogger
import biz.meetmatch.modules.Module
import org.apache.spark.TaskContext

object WithCalcTransactionLogging {
  def apply[B, U<:Module](module: Class[U], category: String, id: String, message: String = "")(f: => B): B = {
    val businessLogger = BusinessLogger.forModule(module)

    val taskContext = TaskContext.get
    businessLogger.transactionStarted(category, id, taskContext.stageId, taskContext.partitionId, taskContext.taskAttemptId, message)
    val result = f
    businessLogger.transactionStopped(category, id)

    result
  }
}
