package com.example.apps

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{Executors, TimeUnit}

import com.example.service.news.YNA
import com.example.service.selenium.SeleniumClient
import com.example.utils.{AppConfig, AppUtils}
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec
import scala.concurrent.duration.Duration
import scala.concurrent.forkjoin.{ForkJoinPool, ForkJoinWorkerThread}
import scala.concurrent.forkjoin.ForkJoinPool.ForkJoinWorkerThreadFactory
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutorService, Future}

object YNAApp extends LazyLogging {
  private implicit val ynaAppThreadPool: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newWorkStealingPool(4))

  private val scheduleExecutorService = Executors.newSingleThreadScheduledExecutor()

  sys.addShutdownHook {
    scheduleExecutorService.shutdown()
    ynaAppThreadPool.shutdown()
  }

  def stream(selenium: SeleniumClient): Unit = {
    val isStreamClose = new AtomicBoolean(false)

    logger.info("start stream.")
    this.updateYNAPolicy(selenium)

    val scheduleExecutorService = Executors.newSingleThreadScheduledExecutor()
    sys.addShutdownHook(scheduleExecutorService.shutdown())

    val lock = AnyRef

    val updatePolicyFuture = scheduleExecutorService.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = if (AppUtils.timeValidationCheck) updateYNAPolicy(selenium)
    }, 0, 1, TimeUnit.HOURS)

    val updateHeadlineNewsFuture = scheduleExecutorService.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = if (AppUtils.timeValidationCheck) updateYNAHeadLineNews(selenium)
    }, 0, 3, TimeUnit.MINUTES)

    val updateHotNewsFuture = scheduleExecutorService.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = if (AppUtils.timeValidationCheck) updateYNAHotNews(selenium)
    }, 0, 5, TimeUnit.MINUTES)

    val updateMostViewRankNewsFuture = scheduleExecutorService.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = if (AppUtils.timeValidationCheck) updateYNAMostViewRankNews(selenium)
    }, 0, 15, TimeUnit.MINUTES)

    val futureList: Future[Boolean] = Future {
      try {
        updatePolicyFuture.get
        updateHeadlineNewsFuture.get
        updateHotNewsFuture.get
        updateMostViewRankNewsFuture.get
      } catch {
        case e: Throwable => logger.error(s"blocking update futures exception. msg: ${e.getMessage}", e)
      } finally {
        isStreamClose.set(true)
        lock.synchronized(lock.notifyAll())
      }

      updatePolicyFuture.isDone &&
        updateHeadlineNewsFuture.isDone &&
        updateHotNewsFuture.isDone &&
        updateMostViewRankNewsFuture.isDone
    }

    @tailrec def loop(maxRetryCount: Int): Int = {
      logger.info(s"update yna news stream future status: $futureList")

      if (maxRetryCount > 0 && !isStreamClose.get) {
        lock.synchronized(lock.wait())
        loop(maxRetryCount - 1)
      } else {
        logger.info("close stream.")
        maxRetryCount
      }
    }

    loop(AppConfig.defaultMaxRetryCount)
    logger.info(s"future status: $futureList")

  }

  def batchAll(selenium: SeleniumClient): Unit = {
    this.updateYNAPolicy(selenium: SeleniumClient)
    val updateYNANewsFuture = Future.sequence(Vector(
      this.updateYNAHeadLineNews(selenium: SeleniumClient),
      this.updateYNAHotNews(selenium: SeleniumClient),
      this.updateYNAMostViewRankNews(selenium: SeleniumClient)
    ))

    Await.result(updateYNANewsFuture, Duration(1, TimeUnit.MINUTES))
  }

  def batchHeadlineNews(selenium: SeleniumClient): Unit = {
    this.updateYNAPolicy(selenium)
    Await.result(this.updateYNAHeadLineNews(selenium), Duration(1, TimeUnit.MINUTES))
  }

  def batchHotNews(selenium: SeleniumClient): Unit = {
    this.updateYNAPolicy(selenium)
    Await.result(this.updateYNAHotNews(selenium), Duration(1, TimeUnit.MINUTES))
  }

  def batchMostViewRankNews(selenium: SeleniumClient): Unit = {
    this.updateYNAPolicy(selenium)
    Await.result(this.updateYNAMostViewRankNews(selenium), Duration(1, TimeUnit.MINUTES))
  }

  private def updateYNAHeadLineNews(selenium: SeleniumClient): Future[(Int, Int)] = {
    if (YNA.isAllowScrapping(YNA.headLineNewsURL)) {
      YNA.updateHeadLineNews(selenium.getBodyElementsWithRetry(YNA.headLineNewsURL)(YNA.isHeadLineNewsEmpty))
    } else {
      logger.error(s"disallow policy. url: ${YNA.headLineNewsURL}")
      Future((0, 0))
    }
  }

  private def updateYNAHotNews(selenium: SeleniumClient): Future[(Int, Int)] = {
    if (YNA.isAllowScrapping(YNA.hotNewsURL)) {
      YNA.updateHotNews(selenium.getBodyElementsWithRetry(YNA.hotNewsURL)(YNA.isHotNewsEmpty))
    } else {
      logger.error(s"disallow policy. url: ${YNA.hotNewsURL}")
      Future((0, 0))
    }
  }

  private def updateYNAMostViewRankNews(selenium: SeleniumClient): Future[(Int, Int)] = {
    if (YNA.isAllowScrapping(YNA.mostViewNewsURL)) {
      YNA.updateMostViewRankNews(selenium.getBodyElementsWithRetry(YNA.mostViewNewsURL)(YNA.isMostViewRankNewsEmpty))
    } else {
      logger.error(s"disallow policy. url: ${YNA.mostViewNewsURL}")
      Future((0, 0))
    }
  }

  def updateYNAPolicy(selenium: SeleniumClient): Unit = {
    YNA.updatePolicy(selenium.getBodyElementsWithRetry(YNA.rootURL + "/robots.txt")(YNA.isPolicyEmpty))
  }
}
