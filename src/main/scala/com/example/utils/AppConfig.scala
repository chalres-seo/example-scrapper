package com.example.utils

import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.DateTime
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration

object AppConfig extends LazyLogging {
  /** read custom application.conf */
//  private val conf: Config = this.readConfigFromFile("conf/application.conf")
  private val conf: Config = ConfigFactory.load().resolve()

  /** application config */
  val applicationName: String = conf.getString("application.name")
  val defaultTimeout: Long = 10
  val defaultTimeUnit: TimeUnit = TimeUnit.SECONDS

  val defaultWaitMillis = 3000L
  val defaultMaxRetryCount = 3

  /** future config */
  val defaultFutureTimeout: Duration = Duration.apply(conf.getLong("application.futureTimeWait"), TimeUnit.MILLISECONDS)
  val modelFutureTimeout: Duration = defaultFutureTimeout

  /** database config, from resource application config */
  lazy val mainDbConfig = {
    logger.debug(s"database config: ${conf.getConfig("database.main")}")
    DatabaseConfig.forConfig[JdbcProfile]("database.main")
  }
  val defaultFetchSize = 10000

  /** encryption config */
  val encryptDefaultKey = "/!default-key!/"

  def timeValidationCheck: Boolean = {
    val currentHour = DateTime.now.getHourOfDay
    logger.info(s"time validation check. current hour: $currentHour")
    currentHour > 6
  }

}
