package com.example.model.entity

import com.example.model.persistent.table.TableRecords
import com.example.utils.AppUtils
import org.hamcrest.CoreMatchers.is
import org.junit.{After, Assert, Before, Test}

trait CommonDaoCRUDTest[R <: TableRecords] {
  lazy val dao: CommonDao[R] = this.getDao

  val exampleDataSetCount: Int = 10
  lazy val exampleDataSet: Vector[R] = this.getCreateExampleDataSet(exampleDataSetCount)

  lazy val exampleRecord: R = this.getExampleRecord
  lazy val exampleRecordUpdated: R = this.getExampleRecordUpdated

  def getDao: CommonDao[R]
  def getCreateExampleDataSet(exampleDataSetCount: Int): Vector[R]

  def getExampleRecord: R
  def getExampleRecordUpdated: R

  @Before
  def setup(): Unit = {
    AppUtils.awaitResultFuture(dao.create())
  }

  @After
  def clenup(): Unit = {
    AppUtils.awaitResultFuture(dao.drop())
  }

  @Test
  def checkDDL(): Unit = {
    dao.getCreateDDL.foreach(println)
  }

  @Test
  def testSingleRecordCRU(): Unit = {
    val insertResult = AppUtils.awaitResultFuture(dao.insert(exampleRecord))
    Assert.assertThat(insertResult.get, is(1))

    val records = this.getAllRecord
    Assert.assertThat(records.length, is(1))
    Assert.assertThat(records.head, is(exampleRecord))

    val updateResult = AppUtils.awaitResultFuture(dao.insertOrUpdateRecord(exampleRecordUpdated))
    Assert.assertThat(updateResult.get, is(1))

    val recordsAfterUpdated = this.getAllRecord
    Assert.assertThat(recordsAfterUpdated.length, is(1))
    Assert.assertThat(recordsAfterUpdated.head, is(exampleRecordUpdated))
  }

  @Test
  def testMultiRecordCR(): Unit = {
    val insertResult = AppUtils.awaitResultFuture(dao.insertAll(exampleDataSet))
    Assert.assertThat(insertResult.get, is(exampleDataSetCount))

    val records = this.getAllRecord
    Assert.assertThat(records.length, is(exampleDataSetCount))
  }

  private def getAllRecord: Vector[R] = {
    val records = AppUtils.awaitResultFuture(dao.selectAllRecord).get.toVector

    println("get all record")
    records.foreach(println)

    records
  }
}
