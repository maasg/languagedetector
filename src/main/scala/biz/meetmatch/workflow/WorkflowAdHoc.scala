package biz.meetmatch.workflow

import biz.meetmatch.modules._

object WorkflowAdHoc extends WorkflowBase {
  override def getModules: Array[Module] = {
    (Array()
      :+ DetectLanguage

      )
  }
}