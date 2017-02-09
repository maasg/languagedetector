package biz.meetmatch.modules.util

import biz.meetmatch.modules.Module
import biz.meetmatch.util.Utils
import com.google.gson.Gson
import org.apache.spark.sql.SparkSession
import org.rogach.scallop.Scallop
import org.slf4j.LoggerFactory

import scalaj.http.Http

object WarmupZeppelin extends Module {

  override def execute(scallopts: Scallop)(implicit sparkSession: SparkSession): Unit = {
    val interpreterId = Utils.getConfig("zeppelin.interpreter.id")

    logger.info(s"Restarting the spark interpreter using url http://localhost:8080/api/interpreter/setting/restart/$interpreterId...")
    val interpreterResponse = Http(s"http://localhost:8080/api/interpreter/setting/restart/$interpreterId")
      .method("put")
      .timeout(connTimeoutMs = 10000, readTimeoutMs = 60000)
      .asParamMap
    logger.info("Response code: " + interpreterResponse.code)

    if (interpreterResponse.code == 200) {
      val bootstrapNotebookId = Utils.getConfig("zeppelin.bootstrapNotebookId.id")

      logger.info(s"Executing the Bootstrap notebook using url http://localhost:8080/api/notebook/job/$bootstrapNotebookId...")
      val notebookResponse = Http(s"http://localhost:8080/api/notebook/job/$bootstrapNotebookId")
        .postForm
        .timeout(connTimeoutMs = 10000, readTimeoutMs = 60000)
        .asParamMap
      logger.info("Response code: " + notebookResponse.code)

      if (notebookResponse.code == 200) {
        def stillRunningParagraphs(): Boolean = {
          logger.info("Checking if the Bootstrap notebook has finished...")
          val checkNotebookResponse = Http("http://localhost:8080/api/notebook/job/2BKFC4D5W")
            .timeout(connTimeoutMs = 10000, readTimeoutMs = 120000)
            .asString
          logger.info("Response code: " + checkNotebookResponse.code)
          val notebookJobResponse = new Gson().fromJson(checkNotebookResponse.body, classOf[GetNotebookJobResponse])
          notebookJobResponse.body.exists(_.status != "FINISHED")
        }

        while (stillRunningParagraphs()) {
          logger.info("Keep on polling...")
          Thread.sleep(5000)
        }
        logger.info("The Bootstrap notebook has finished.")
      }
    }
  }

  case class GetNotebookJobResponse(status: String, body: Array[ParagraphStatus])

  case class ParagraphStatus(id: String, started: String, finished: String, status: String)

  override protected val logger = LoggerFactory.getLogger(this.getClass)
}

