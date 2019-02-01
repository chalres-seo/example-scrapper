package com.example.model.persistent.db

import java.sql.Timestamp

import com.typesafe.scalalogging.LazyLogging
import org.joda.time.DateTime
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcBackend, JdbcProfile}

trait DBConfiguration extends LazyLogging with DBIOActionHandler {
  /** abstract member for get database config on runtime */
  val config: DatabaseConfig[JdbcProfile]
  val profile: JdbcProfile = config.profile

  import profile.api._

  /** implicit type convert */
  implicit val yodaDateTimeRepType: profile.BaseColumnType[DateTime] = profile.MappedColumnType.base[ DateTime, Timestamp ] (
    dt => new Timestamp( dt.getMillis ),
    ts => new DateTime( ts.getTime )
  )

  val getResultToResultSet = GetResult(_.rs)

  def getDBConnection: JdbcBackend#DatabaseDef = {
    logger.info("get database connection.")
    config.db
  }

  def closeAllDBConnection(): Unit = {
    logger.info("close database connection.")
    config.db.close()
  }
}