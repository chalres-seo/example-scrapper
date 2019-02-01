package com.example.model.entity

import com.example.model.persistent.table.{YNALastNewsLinkTable, YNALastNewsLinkRecord => _Record}
import com.example.utils.AppConfig
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.DateTime
import slick.basic.{DatabaseConfig, DatabasePublisher}
import slick.jdbc.{JdbcProfile, ResultSetConcurrency, ResultSetType}

import scala.concurrent.Future
import scala.util.Try

class YNALastNewsLinkDao(val config: DatabaseConfig[JdbcProfile]) extends LazyLogging with CommonDao[_Record] with YNALastNewsLinkTable {
  import config.profile.api._

  private val dbConn = this.getDBConnection

  /** common action */

  override def getCreateDDL: Iterator[String] = tableQuery.schema.createStatements
  override def getDropDDL: Iterator[String] = tableQuery.schema.dropStatements
  override def truncateDDL: Iterator[String] = tableQuery.schema.truncateStatements

  override def create(): Future[Try[Unit]] = {
    logger.info(s"create table. table name: $tableName")
    dbConn.run(tableQuery.schema.create.asTry)
      .map(runTryHandler(_, s"create table $tableName"))
  }

  override def drop(): Future[Try[Unit]] = {
    logger.info(s"drop table. table name: $tableName")
    dbConn.run(tableQuery.schema.drop.asTry)
      .map(runTryHandler(_, s"drop table $tableName"))
  }

  override def truncate(): Future[Try[Unit]] = {
    logger.info(s"truncate table. table name: $tableName")
    dbConn.run(tableQuery.schema.truncate.asTry)
      .map(runTryHandler(_, s"truncate table $tableName"))
  }

  override def insert(record: _Record): Future[Try[Int]] = {
    logger.info(s"insert or update record. table name: $tableName")
    logger.debug(s"record info: $record")
    dbConn.run(tableQuery.forceInsert(record).asTry)
      .map(runTryHandler(_, "insert single record"))
  }

  override def insertOrUpdateRecord(record: _Record): Future[Try[Int]] = {
    logger.info(s"insert or update record. table name: $tableName")
    logger.debug(s"record info: $record")
    dbConn.run(tableQuery.insertOrUpdate(record).asTry)
      .map(runTryHandler(_, "insert or update record"))
  }

  override def insertAll(records: Vector[_Record]): Future[Try[Int]] = {
    logger.info(s"insert multiple records. record count: ${records.length}, table name: $tableName")
    logger.debug(s"records info:\n\t|${records.mkString("\n\t|")}")
    dbConn.run(tableQuery.forceInsertAll(records).transactionally.asTry)
      .map(runTryHandler[Option[Int], Int](_, "insert multiple records")(r => r.get))
  }

  override def selectAllRecord: Future[Try[Seq[_Record]]] = {
    logger.info(s"select all record. table name: $tableName")
    dbConn.run(tableQuery.result.asTry)
      .map(runTryHandler(_, "select all records"))
  }

  override def streamRecords(fetchRowSize: Int = AppConfig.defaultFetchSize): DatabasePublisher[_Record] = {
    logger.info(s"stream records. table name: $tableName")
    dbConn.stream(tableQuery
      .result
      .withStatementParameters(
        rsType = ResultSetType.ForwardOnly,
        rsConcurrency = ResultSetConcurrency.ReadOnly,
        fetchSize = fetchRowSize)
      .transactionally)
  }

  override def closeDbConn(): Unit = {
    logger.info("close database connection")
    dbConn.close()
  }

  /** custom action */

  def selectLastLink(newsUrl: String): Future[Try[Seq[_Record]]] = {
    logger.info(s"select news last link record. newsUrl: $newsUrl, table name: $tableName")

    dbConn.run(tableQuery.filter(_.newsUrl === newsUrl).take(1).result.asTry)
      .map(runTryHandler(_, "select record."))
  }

  def updateLastLink(newsUrl: String, link: String): Future[Try[Int]] = {
    logger.info(s"update news last link. news url: $newsUrl, table name: $tableName")

    this.insertOrUpdateRecord(_Record(newsUrl, link, DateTime.now))
  }
}

object YNALastNewsLinkDao extends LazyLogging {
  private lazy val instance = this.createInstance

  def getInstance: YNALastNewsLinkDao = this.instance

  private def createInstance: YNALastNewsLinkDao = {
    val instance = new YNALastNewsLinkDao(AppConfig.mainDbConfig)
    logger.info(s"create ${instance.getClass.getSimpleName} object.")
    instance
  }
}