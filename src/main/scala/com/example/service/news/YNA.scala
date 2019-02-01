package com.example.service.news

import java.util.concurrent.TimeUnit

import com.example.model.entity.{YNAHeadLineDao, YNAHotDao, YNALastNewsLinkDao, YNAMostViewRankDao}
import com.example.model.persistent.table.{YNAHeadLineNewsRecord, YNAHotNewsRecord, YNALastNewsLinkRecord, YNAMostViewRankNewsRecord}
import com.example.utils.{AppConfig, AppUtils, SendSlackMsg}
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.DateTime
import org.jsoup.nodes.Element

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.{Failure, Success, Try}
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object YNA extends LazyLogging {
  //updatedRecordCount, totalNewRecords
  type UpdateRecordInfo = (Int, Int)

  @volatile private lazy val policy: mutable.Map[String, Boolean] = mutable.Map.empty[String, Boolean]

  val rootURL = "https://www.yna.co.kr"

  val headLineNewsURL = "https://www.yna.co.kr/theme/headlines"
  val hotNewsURL = "https://www.yna.co.kr/theme/hotnews"
  val mostViewNewsURL = "https://www.yna.co.kr/theme/mostviewed/index"

  @volatile private var lastHeadlineNewsLink: String = this.getLastHeadlineNewsLink
  @volatile private var lastHotNewsLink: String = this.getLastHotNewsLink
  @volatile private var lastMostViewRankNewsList: Seq[String] = this.getLastMostViewRankNewsList

  def isPolicyEmpty(bodyDocument: Element): Boolean = {
    bodyDocument.text().isEmpty
  }

  def isHeadLineNewsEmpty: Element => Boolean = { bodyDocument: Element =>
    bodyDocument.select("li.emg").isEmpty
  }

  def isHotNewsEmpty: Element => Boolean = { bodyDocument: Element =>
    bodyDocument.select("li.emg").isEmpty
  }

  def isMostViewRankNewsEmpty: Element => Boolean = { bodyDocument: Element =>
    bodyDocument.select("li.section").isEmpty
  }

  def updatePolicy(bodyDocument: Element): Unit = {
    logger.info("update policy.")
    val robotsTxt = bodyDocument.text()

    if (robotsTxt != "") {
      policy.clear()

      val robotsTxtLines = robotsTxt.split("\n").toVector
      val policyGroupStartLineNumbers = robotsTxtLines.zipWithIndex.filter(_._1.startsWith("User-agent:"))

      val policyStartLineNumber = Try(policyGroupStartLineNumbers.filter(_._1.equals("User-agent: *")).map(_._2).head).toOption
      val policyEndLineNumber = policyStartLineNumber.map { startLineNumber =>
        if (policyGroupStartLineNumbers.length > 1) {
          policyGroupStartLineNumbers.filter(_._2 > startLineNumber).map(_._2).head - 1
        } else robotsTxtLines.length - 1
      }

      (policyStartLineNumber, policyEndLineNumber) match {
        case (Some(s), Some(e)) =>
          robotsTxtLines.slice(s, e).filter(_ != "").foreach { line =>
            val splitLine = line.replace(" ", "").split(":")

            splitLine(0) match {
              case "Allow" =>
                policy.put(rootURL + splitLine(1), true)
              case "Disallow" =>
                policy.put(rootURL + splitLine(1), false)
              case _ => Unit
            }
          }
        case _ => Unit
      }
    }
  }

  def updateHeadLineNews(bodyDocument: Element): Future[UpdateRecordInfo] = {
    logger.info("update headline news.")

    val taskFuture: Future[(Int, Int)] = Future(this.parseHeadLineNewsBody(bodyDocument)).map { newsRecords =>
      if (newsRecords.nonEmpty) {
        val dbIOActions: Vector[Future[Try[Int]]] = newsRecords.map(YNAHeadLineDao.getInstance.insertIfNotExist)
        val dbIOActionResult: Future[Int] = Future.sequence(dbIOActions).map(_.map(_.toOption.getOrElse(0)).sum)
        (Await.result(dbIOActionResult, AppConfig.defaultFutureTimeout), newsRecords.length)
      } else {
        (0, newsRecords.length)
      }
    }

    taskFuture.onComplete {
      case Success(updatedRecordInfo) =>
        val (updatedRecordCount, newsRecordsCount) = updatedRecordInfo
        val duplicatedRecordCount = newsRecordsCount - updatedRecordCount

        if (updatedRecordCount > 0) {
          logger.info(s"headline news updated. " +
            s"update record count: $updatedRecordCount/$newsRecordsCount, " +
            s"duplicated or error record count: $duplicatedRecordCount")
        } else {
          logger.info(s"headline news is not updated.")
        }

      case Failure(e) =>
        SendSlackMsg.send("failed headline news update.")
        logger.error(s"failed headline news update. msg: ${e.getMessage}", e)
    }

    taskFuture
  }

  def updateHotNews(bodyDocument: Element): Future[UpdateRecordInfo] = {
    logger.info("update hot news.")

    val taskFuture: Future[(Int, Int)] = Future(this.parseHotNewsBody(bodyDocument)).map { newsRecords =>
      if (newsRecords.nonEmpty) {
        val dbIOActions: Vector[Future[Try[Int]]] = newsRecords.map(YNAHotDao.getInstance.insertIfNotExist)
        val dbIOActionResult: Future[Int] = Future.sequence(dbIOActions).map(_.map(_.toOption.getOrElse(0)).sum)
        (Await.result(dbIOActionResult, AppConfig.defaultFutureTimeout), newsRecords.length)
      } else {
        (0, newsRecords.length)
      }
    }

    taskFuture.onComplete {
      case Success(updatedRecordInfo) =>
        val (updatedRecordCount, newsRecordsCount) = updatedRecordInfo
        val duplicatedRecordCount = newsRecordsCount - updatedRecordCount

        if (updatedRecordCount > 0) {
          logger.info(s"hot news updated. " +
            s"update record count: $updatedRecordCount/$newsRecordsCount, " +
            s"duplicated or error record count: $duplicatedRecordCount")
        } else {
          logger.info(s"hot news is not updated.")
        }
      case Failure(e) =>
        SendSlackMsg.send("failed hot news update.")
        logger.error(s"failed hot news update. msg: ${e.getMessage}", e)
    }

    taskFuture
  }

  def updateMostViewRankNews(bodyDocument: Element): Future[UpdateRecordInfo] = {
    logger.info("update most view rank news.")
    val taskFuture = Future(this.parseMostViewRankNewsBody(bodyDocument).toVector).map { newsRecords =>
      if (newsRecords.nonEmpty) {
        val dbIOActionResult: Future[Int] = YNAMostViewRankDao.getInstance.insertAll(newsRecords).map(_.getOrElse(0))
        (Await.result(dbIOActionResult, AppConfig.defaultFutureTimeout), newsRecords.length)
      } else (0, newsRecords.length)
    }

    taskFuture.onComplete {
      case Success(updatedRecordInfo) =>
        val (updatedRecordCount, newsRecordsCount) = updatedRecordInfo
        val duplicatedRecordCount = newsRecordsCount - updatedRecordCount

        if (updatedRecordCount > 0) {
          logger.info(s"most view rank news updated. " +
            s"update record count: $updatedRecordCount/$newsRecordsCount, " +
            s"duplicated or error record count: $duplicatedRecordCount")
        } else {
          logger.info("most view rank news is not updated.")
        }

      case Failure(e) =>
        SendSlackMsg.send("failed most view rank news update.")
        logger.error(s"failed most view rank news update. msg: ${e.getMessage}", e)
    }

    taskFuture
  }

  private def parseHeadLineNewsBody(bodyDocument: Element): Vector[YNAHeadLineNewsRecord] = {
    logger.info("parse headline news body element.")
    val dateTime = DateTime.now

    val source = bodyDocument
      .getElementById("themeNewsList")

    logger.info(s"headline news source size: ${source.toString.length.toString}")

    val headLineNewsRecords: Vector[YNAHeadLineNewsRecord] = source.getElementsByClass("emg").toVector.map { emg =>
      val title = emg.getElementsByClass("news-tl").text()
      val lead = emg.getElementsByClass("lead")
      val link = lead.select("a").attr("href")
      val summary = lead.select("a").text()
      val createDateTime = DateTime.parse(lead.select("span").text().replace(" ", "T"))

      YNAHeadLineNewsRecord(link, dateTime, title, summary, createDateTime)
    }

    logger.info(s"headline news record count: ${headLineNewsRecords.length}")

    val newHeadLineNewsRecords: Vector[YNAHeadLineNewsRecord] =
      if (lastHeadlineNewsLink != "" && headLineNewsRecords.nonEmpty) {
        val newHeadLineNewsRecordsLastIndex = Try {
          headLineNewsRecords.zipWithIndex
            .filter(_._1.link == lastHeadlineNewsLink)
            .head._2
        }.toOption
          .getOrElse(headLineNewsRecords.length)

        headLineNewsRecords.slice(0, newHeadLineNewsRecordsLastIndex)
      } else headLineNewsRecords

    logger.info(s"new headline news record: count: ${newHeadLineNewsRecords.length}")

    if (newHeadLineNewsRecords.nonEmpty) {
      logger.info(s"last headline news link updated. before: $lastHeadlineNewsLink, after: ${headLineNewsRecords.head.link}")
      lastHeadlineNewsLink = headLineNewsRecords.head.link
      YNALastNewsLinkDao.getInstance.updateLastLink(headLineNewsURL, lastHeadlineNewsLink)
    } else logger.info(s"last headline news link not updated. current: $lastHeadlineNewsLink")

    newHeadLineNewsRecords
  }

  private def parseHotNewsBody(bodyDocument: Element): Vector[YNAHotNewsRecord] = {
    logger.info("parse hot news body element.")
    val dateTime = DateTime.now

    val source = bodyDocument
      .getElementById("themeNewsList")

    logger.info(s"hot news source size: ${source.toString.length.toString}")

    val hotNewsRecords: Vector[YNAHotNewsRecord] = source.getElementsByClass("emg").toVector.map { emg =>
      val title = emg.getElementsByClass("news-tl").text()
      val lead = emg.getElementsByClass("lead")
      val link = lead.select("a").attr("href")
      val summary = lead.select("a").text()
      val createDateTime = DateTime.parse(lead.select("span").text().replace(" ", "T"))

      YNAHotNewsRecord(link, dateTime, title, summary, createDateTime)
    }

    logger.info(s"hot news record count: ${hotNewsRecords.length}")

    val newHotNewsRecords: Vector[YNAHotNewsRecord] =
      if (lastHotNewsLink != "" && hotNewsRecords.nonEmpty) {
        val newHotNewsRecordsLastIndex = Try {
          hotNewsRecords.zipWithIndex
            .filter(_._1.link == lastHotNewsLink)
            .head._2
        }.toOption
          .getOrElse(hotNewsRecords.length)

        hotNewsRecords.slice(0, newHotNewsRecordsLastIndex)
      } else hotNewsRecords

    logger.info(s"new hot news record count: ${newHotNewsRecords.length}")

    if (newHotNewsRecords.nonEmpty) {
      logger.info(s"last hot news link updated. before: $lastHotNewsLink, after: ${hotNewsRecords.head.link}")
      lastHotNewsLink = hotNewsRecords.head.link
      YNALastNewsLinkDao.getInstance.updateLastLink(hotNewsURL, lastHotNewsLink)
    } else logger.info(s"last hot news link not updated. current: $lastHotNewsLink")

    newHotNewsRecords
  }

  private def parseMostViewRankNewsBody(bodyDocument: Element): Seq[YNAMostViewRankNewsRecord] = {
    logger.info("parse most view rank news.")
    val dateTime = DateTime.now

    val source = bodyDocument
      .getElementById("themeNewsList")

    logger.info(s"most view news source size: ${source.toString.length.toString}")

    val mostViewRankNewsRecords: Seq[YNAMostViewRankNewsRecord] = source.getElementsByClass("section").map { section =>
      val rank = section.getElementsByClass("num").text().toInt
      val title = section.getElementsByClass("news-tl").text()
      val lead = section.getElementsByClass("lead")
      val link = lead.select("a").attr("href")
      val summary = lead.select("a").text()
      val createDateTime = DateTime.parse(lead.select("span").text().replace(" ", "T"))

      YNAMostViewRankNewsRecord(dateTime, rank, link, title, summary, createDateTime)
    }

    logger.info(s"most view rank news record count: ${mostViewRankNewsRecords.length}")

    if (lastMostViewRankNewsList.isEmpty && mostViewRankNewsRecords.nonEmpty) {
      lastMostViewRankNewsList = mostViewRankNewsRecords.map(_.link)
      mostViewRankNewsRecords
    } else {
      val isRankChanged = mostViewRankNewsRecords
        .zip(lastMostViewRankNewsList)
        .exists(zippedRecord => zippedRecord._1.link != zippedRecord._2)

      if (isRankChanged) {
        lastMostViewRankNewsList = mostViewRankNewsRecords.map(_.link)
        mostViewRankNewsRecords
      } else Seq.empty
    }
  }

  private def getLastHeadlineNewsLink: String = {
    Await.result(YNALastNewsLinkDao.getInstance.selectLastLink(headLineNewsURL), AppConfig.defaultFutureTimeout) match {
      case Success(newsLink) => if (newsLink.isEmpty) "" else newsLink.head.link
      case Failure(_) => ""
    }
  }

  private def getLastHotNewsLink: String = {
    Await.result(YNALastNewsLinkDao.getInstance.selectLastLink(hotNewsURL), AppConfig.defaultFutureTimeout) match {
      case Success(newsLink) => if (newsLink.isEmpty) "" else newsLink.head.link
      case Failure(_) => ""
    }
  }

  private def getLastMostViewRankNewsList: Seq[String] = {
    Await.result(YNAMostViewRankDao.getInstance.selectLastRankNewsList, AppConfig.defaultFutureTimeout) match {
      case Success(newsLinkList) => newsLinkList.map(_.link)
      case Failure(_) => Seq.empty
    }
  }

  def isAllowScrapping: Boolean = {
    policy(rootURL + "/")
  }

  def isAllowScrapping(url: String): Boolean = {
    policy.filter(_._2 == false).foreach { disallowUrl =>
      if (url.contains(disallowUrl))
        return false
    }
    true
  }
}