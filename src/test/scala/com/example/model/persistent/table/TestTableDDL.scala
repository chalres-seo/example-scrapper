package com.example.model.persistent.table

import com.example.model.entity.{YNAHeadLineDao, YNAHotDao, YNAMostViewRankDao}
import org.junit.Test

class TestTableDDL {
  @Test
  def testTableDDL(): Unit = {
    YNAHeadLineDao.getInstance.getCreateDDL.foreach(println)
    YNAHotDao.getInstance.getCreateDDL.foreach(println)
    YNAMostViewRankDao.getInstance.getCreateDDL.foreach(println)
  }
}
