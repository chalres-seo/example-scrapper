package com.example.model.persistent.db

import com.example.utils.SendSlackMsg
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Try}

trait DBIOActionHandler extends LazyLogging {
  def runTryHandler[A, B](runTry: Try[A], logMsg: String)(whenSuccess: A => B): Try[B] = {
    this.runTry(runTry, logMsg).map { runResult =>
      logger.info(s"DB IO action succeed $logMsg.")
      whenSuccess(runResult)
    }
  }

  def runTryHandler[A](runTry: Try[A], logMsg: String): Try[A] = {
    this.runTry(runTry, logMsg).map { runResult =>
      logger.info(s"DB IO action succeed $logMsg.")
      runResult
    }
  }

  private def runTry[A](runTry: Try[A], logMsg: String): Try[A] = {
    runTry.recoverWith {
      case e: Exception =>
        logger.error(s"DB IO action failed $logMsg, msg: ${e.getMessage}", e)
        SendSlackMsg.send("DB IO action failed.")
        Failure(e)
    }
  }
}
