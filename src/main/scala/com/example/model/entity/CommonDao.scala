package com.example.model.entity

import java.util.concurrent.Executors

import com.example.model.persistent.db.DBConfiguration
import com.example.model.persistent.table.TableRecords
import com.example.utils.AppConfig
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.typesafe.scalalogging.LazyLogging
import slick.basic.DatabasePublisher

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}
import scala.util.Try

trait CommonDao[R <: TableRecords] extends LazyLogging with DBConfiguration {
  implicit val dbActionThreadPool: ExecutionContextExecutorService = CommonDao.dbActionThreadPool

  /** common ddl action */
  def getCreateDDL: Iterator[String]
  def getDropDDL: Iterator[String]
  def truncateDDL: Iterator[String]
  def create(): Future[Try[Unit]]
  def drop(): Future[Try[Unit]]
  def truncate(): Future[Try[Unit]]

  /** common io action */
  def insert(record: R): Future[Try[Int]]
  def insertAll(records: Vector[R]): Future[Try[Int]]
  def insertOrUpdateRecord(record: R): Future[Try[Int]]
  def selectAllRecord: Future[Try[Seq[R]]]
  def streamRecords(fetchRowSize: Int = AppConfig.defaultFetchSize): DatabasePublisher[R]
  //def deleteRecord(record: R): Future[Try[Int]]

  def closeDbConn(): Unit
}

private object CommonDao {
  private val dbActionThreadPool: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newWorkStealingPool(4))
  sys.addShutdownHook(dbActionThreadPool.shutdown())
}