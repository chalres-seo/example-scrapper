package com.example.model.entity

import com.example.model.persistent.table.{YNALastNewsLinkRecord, YNAMostViewRankNewsRecord}
import org.joda.time.DateTime

class TestYNALastNewsLinkDao extends CommonDaoCRUDTest[YNALastNewsLinkRecord] {
  override def getDao: CommonDao[YNALastNewsLinkRecord] = YNALastNewsLinkDao.getInstance

  override def getCreateExampleDataSet(exampleDataSetCount: Int): Vector[YNALastNewsLinkRecord] = {
    (1 to exampleDataSetCount).map(k =>
      YNALastNewsLinkRecord(s"name-$k", s"link-$k", DateTime.now)
    ).toVector
  }

  override def getExampleRecord: YNALastNewsLinkRecord = {
    YNALastNewsLinkRecord(s"name-99", s"link-98", DateTime.now)
  }

  override def getExampleRecordUpdated: YNALastNewsLinkRecord = {
    YNALastNewsLinkRecord(s"name-99", s"link-99", DateTime.now)
  }
}
