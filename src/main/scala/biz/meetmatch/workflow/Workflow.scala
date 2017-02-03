package biz.meetmatch.workflow

import biz.meetmatch.modules._

object Workflow extends WorkflowBase {
  override def getModules: Array[Module] = {
    (Array()
      :+ DetectLanguage
      :+ CountSentencesByLanguage
      )
  }
}