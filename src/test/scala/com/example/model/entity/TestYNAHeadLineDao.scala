package com.example.model.entity

import com.example.model.persistent.table.YNAHeadLineNewsRecord
import org.joda.time.DateTime

class TestYNAHeadLineDao extends CommonDaoCRUDTest[YNAHeadLineNewsRecord]{
  override def getDao: CommonDao[YNAHeadLineNewsRecord] = YNAHeadLineDao.getInstance

  override def getCreateExampleDataSet(exampleDataSetCount: Int): Vector[YNAHeadLineNewsRecord] = {
    (1 to exampleDataSetCount).map(k =>
      YNAHeadLineNewsRecord(s"link-$k", DateTime.now, s"title-$k", "summary-$k", DateTime.now)
    ).toVector
  }

  override def getExampleRecord: YNAHeadLineNewsRecord = {
    YNAHeadLineNewsRecord("link-99", DateTime.now, s"title-98", "summary-98", DateTime.now.minusDays(1))
  }

  override def getExampleRecordUpdated: YNAHeadLineNewsRecord = {
    YNAHeadLineNewsRecord("link-99", DateTime.now, s"title-99", "summary-99", DateTime.now)
  }
}
