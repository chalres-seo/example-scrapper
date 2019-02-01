package com.example.model.persistent.table

import com.example.model.persistent.db.DBConfiguration
import org.joda.time.DateTime
import slick.lifted.ProvenShape

trait YNAMostViewRankTable extends DBConfiguration {
  import profile.api._

  val tableName: String = "TB_YNA_MOST_VIEW_RANK_NEWS"

  lazy val tableQuery: TableQuery[YNAMostViewRank] = TableQuery[YNAMostViewRank]

  protected class YNAMostViewRank(tag: Tag) extends Table[YNAMostViewRankNewsRecord](tag, tableName) {
    def dateTime: Rep[DateTime] = column[DateTime]("datetime")
    def rank: Rep[Int] = column[Int]("rank")
    def link: Rep[String] = column[String]("link", O.Length(128))
    def title: Rep[String] = column[String]("title", O.Length(256))
    def summary: Rep[String] = column[String]("summary", O.Length(256))
    def publishingDateTime: Rep[DateTime] = column[DateTime]("publishing_datetime")
    def pk = primaryKey(s"${tableName}_PK", (dateTime, rank))
    override def * : ProvenShape[YNAMostViewRankNewsRecord] =
      (dateTime, rank, link, title, summary, publishingDateTime) <>
        (YNAMostViewRankNewsRecord.tupled, YNAMostViewRankNewsRecord.unapply)
  }
}

case class YNAMostViewRankNewsRecord(dateTime: DateTime,
                                     rank: Int,
                                     link: String,
                                     title: String,
                                     summary: String,
                                     publishingDateTime: DateTime) extends TableRecords