package com.example.apps

import java.awt.image.BufferedImage
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicBoolean

import com.example.service.news.YNA
import com.example.service.selenium.SeleniumClient
import com.example.utils.AppConfig
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.DateTime

object Application extends LazyLogging {
  private val selenium: SeleniumClient = SeleniumClient.createSeleniumClient

  def main(args: Array[String]): Unit = {
    logger.info("start app.")

    val cmdList = Vector(
      "yna_stream",
      "yna_batch_all",
      "yna_headline_news",
      "yna_hot_news",
      "yna_most_view_rank_news"
    )

    require(args.length == 1, s"require 1 argument. ${cmdList.mkString(", ")}")

    args.head match {
      case "yna_stream" => YNAApp.stream(selenium)
      case "yna_batch_all" => YNAApp.batchAll(selenium)
      case "yna_headline_news" => YNAApp.batchHeadlineNews(selenium)
      case "yna_hot_news" => YNAApp.batchHotNews(selenium)
      case "yna_most_view_rank_news" => YNAApp.batchMostViewRankNews(selenium)
      case unknown => logger.error(s"unknown argument. $unknown")
    }
  }

  def close(): Unit = {
    selenium.close()
    Thread.sleep(3000)
  }
}