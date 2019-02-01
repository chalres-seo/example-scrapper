package com.example.apps

import java.awt.Graphics2D
import java.awt.image.BufferedImage

import com.example.model.entity.YNAHeadLineDao
import com.example.model.persistent.table.YNAHeadLineNewsRecord
import org.joda.time.DateTime

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object TestProdApp {
  def main(args: Array[String]): Unit = {
    val records = Vector(
      YNAHeadLineNewsRecord("/view/AKR20190117050300001", DateTime.now, "", "", DateTime.now),
      YNAHeadLineNewsRecord("/view/AKR20190117060300002", DateTime.now, "", "", DateTime.now),
      YNAHeadLineNewsRecord("/view/AKR20190117050600001", DateTime.now, "", "", DateTime.now),
      YNAHeadLineNewsRecord("/view/AKR20190117066500004", DateTime.now, "", "", DateTime.now),
      YNAHeadLineNewsRecord("/view/AKR20190117066200009", DateTime.now, "", "", DateTime.now)
    )

    val findNotExistRecords: Future[Vector[YNAHeadLineNewsRecord]] = YNAHeadLineDao.getInstance.insertIfNotExist(records).map {
      case Success(resultSet) =>
        records.filter(r => !resultSet.map(_.link).contains(r.link))
      case Failure(exception) =>
        Vector.empty
    }

    val notExistRecords: Vector[YNAHeadLineNewsRecord] = Await.result(findNotExistRecords, Duration.Inf)

    notExistRecords.foreach(println)
  }
}


