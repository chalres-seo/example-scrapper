package com.example.model.entity

import com.example.model.persistent.table.{YNAMostViewRankTable, YNAMostViewRankNewsRecord => _Record}
import com.example.utils.AppConfig
import com.typesafe.scalalogging.LazyLogging
import slick.basic.{DatabaseConfig, DatabasePublisher}
import slick.jdbc.{JdbcProfile, ResultSetConcurrency, ResultSetType}

import scala.concurrent.Future
import scala.util.Try

class YNAMostViewRankDao(val config: DatabaseConfig[JdbcProfile]) extends LazyLogging with CommonDao[_Record] with YNAMostViewRankTable {
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
      .map(runTryHandler(_, "select all records."))
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

  def selectLastRankNewsList: Future[Try[Seq[_Record]]] = {
    logger.info("select last rank news list.")
    val query = tableQuery
      .sortBy(r => (r.dateTime.desc, r.rank.asc))
      .take(10)

    dbConn.run(query.result.asTry)
      .map(runTryHandler(_, "select last rank news list."))
  }
}

object YNAMostViewRankDao extends LazyLogging {
  private lazy val instance = this.createInstance

  def getInstance: YNAMostViewRankDao = this.instance

  private def createInstance: YNAMostViewRankDao = {
    val instance = new YNAMostViewRankDao(AppConfig.mainDbConfig)
    logger.info(s"create ${instance.getClass.getSimpleName} object.")
    instance
  }
}