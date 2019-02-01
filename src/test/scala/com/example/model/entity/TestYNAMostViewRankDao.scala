package com.example.model.entity

import com.example.model.persistent.table.YNAMostViewRankNewsRecord
import org.joda.time.DateTime

class TestYNAMostViewRankDao extends CommonDaoCRUDTest[YNAMostViewRankNewsRecord] {
  override def getDao: CommonDao[YNAMostViewRankNewsRecord] = YNAMostViewRankDao.getInstance

  override def getCreateExampleDataSet(exampleDataSetCount: Int): Vector[YNAMostViewRankNewsRecord] = {
    (1 to exampleDataSetCount).map(k =>
      YNAMostViewRankNewsRecord(DateTime.now, k, s"link-$k", s"title-$k", "summary-$k", DateTime.now)
    ).toVector
  }

  override def getExampleRecord: YNAMostViewRankNewsRecord = {
    YNAMostViewRankNewsRecord(DateTime.parse("2019-01-01"), 99, s"link-99", s"title-98", "summary-98", DateTime.now)
  }

  override def getExampleRecordUpdated: YNAMostViewRankNewsRecord = {
    YNAMostViewRankNewsRecord(DateTime.parse("2019-01-01"), 99, s"link-99", s"title-99", "summary-99", DateTime.now)
  }
}
