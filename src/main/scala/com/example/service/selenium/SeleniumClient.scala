package com.example.service.selenium

import java.util.concurrent.TimeUnit

import com.example.utils.AppConfig
import com.typesafe.scalalogging.LazyLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.remote.RemoteWebDriver

class SeleniumClient(webDriver: RemoteWebDriver) extends LazyLogging {
  private val MAX_RETRY_COUNT = AppConfig.defaultMaxRetryCount
  private val beforeScrappingTimeWait = AppConfig.defaultWaitMillis

  def getBodyElements(url: String): Element = {
    logger.info("wait 3sec before get url page source..")
    Thread.sleep(beforeScrappingTimeWait)

    logger.info(s"get url page source. url: $url")
    try {
      webDriver.get(url)
    } catch {
      case e: Exception =>
        logger.error(s"failed get url page source. url: $url, msg: ${e.getMessage}", e)
        new Element("empty")
    }

    val html = webDriver.getPageSource
    logger.info(s"page source result size: ${html.length}")

    Jsoup.parse(html).body()
  }

  def getBodyElementsWithRetry(url: String)(checkElementEmpty: Element => Boolean): Element = {
    def loop(maxRetryCount: Int): Element = {
      val bodyElement = this.getBodyElements(url)

      if (maxRetryCount > 0 && checkElementEmpty(bodyElement)) {
        logger.error(s"failed get body elements validation check. remain retry count $maxRetryCount")
        loop(maxRetryCount - 1)
      } else bodyElement
    }

    loop(MAX_RETRY_COUNT)
  }

  def close(): Unit = {
    logger.info("close selenium driver.")
    try {
      webDriver.close()
      webDriver.quit()
    } catch {
      case e: org.openqa.selenium.WebDriverException =>
        logger.warn(s"already close selenium driver. exception msg: ${e.getMessage}")
      case e: Exception =>
        logger.error(s"failed close selenium driver. exception msg: ${e.getMessage}", e)
        logger.error(e.getClass.toString)
      case t: Throwable =>
        logger.error(s"failed close selenium driver. thrown msg: ${t.getMessage}", t)
    }
  }
}

object SeleniumClient extends LazyLogging {
  System.setProperty("webdriver.chrome.driver", "webdriver/chromedriver")

  private val DRIVER_MAX_WAIT_TIME = 30
  private val DRIVER_HEADLESS = true

  private val webDriverOptions = new ChromeOptions()
  webDriverOptions.setHeadless(DRIVER_HEADLESS)

  def createSeleniumClient: SeleniumClient = {
    val seleniumClient = new SeleniumClient(this.createWebDriver)

    sys.addShutdownHook(seleniumClient.close())
    seleniumClient
  }

  private def createWebDriver: RemoteWebDriver = {
    logger.info("create webDriver")
    val webDriver = new ChromeDriver(webDriverOptions)
    webDriver.manage().timeouts().implicitlyWait(DRIVER_MAX_WAIT_TIME, TimeUnit.SECONDS)

    webDriver
  }
}