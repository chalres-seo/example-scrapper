package com.example.model.entity

import com.example.model.persistent.table.YNAHotNewsRecord
import org.joda.time.DateTime

class TestYNAHotDao extends CommonDaoCRUDTest[YNAHotNewsRecord] {
  override def getDao: CommonDao[YNAHotNewsRecord] = YNAHotDao.getInstance

  override def getCreateExampleDataSet(exampleDataSetCount: Int): Vector[YNAHotNewsRecord] = {
    (1 to exampleDataSetCount).map(k =>
      YNAHotNewsRecord(s"link-$k", DateTime.now, s"title-$k", "summary-$k", DateTime.now)
    ).toVector
  }

  override def getExampleRecord: YNAHotNewsRecord = {
    YNAHotNewsRecord("link-99", DateTime.now, s"title-98", "summary-98", DateTime.now.minusDays(1))
  }

  override def getExampleRecordUpdated: YNAHotNewsRecord = {
    YNAHotNewsRecord("link-99", DateTime.now, s"title-99", "summary-99", DateTime.now)
  }
}
