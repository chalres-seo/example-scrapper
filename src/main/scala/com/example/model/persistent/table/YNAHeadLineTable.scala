package com.example.model.persistent.table

import com.example.model.persistent.db.DBConfiguration
import org.joda.time.DateTime
import slick.lifted.ProvenShape

trait YNAHeadLineTable extends DBConfiguration {
  import profile.api._

  val tableName: String = "TB_YNA_HEADLINE_NEWS"

  lazy val tableQuery: TableQuery[YNAHeadLine] = TableQuery[YNAHeadLine]

  protected class YNAHeadLine(tag: Tag) extends Table[YNAHeadLineNewsRecord](tag, tableName) {
    def link: Rep[String] = column[String]("link", O.Length(128))
    def dateTime: Rep[DateTime] = column[DateTime]("datetime")
    def title: Rep[String] = column[String]("title", O.Length(256))
    def summary: Rep[String] = column[String]("summary", O.Length(256))
    def publishingDateTime: Rep[DateTime] = column[DateTime]("publishing_datetime")
    def pk = primaryKey(tableName + "_PK", link)
    override def * : ProvenShape[YNAHeadLineNewsRecord] =
      (link, dateTime, title, summary, publishingDateTime) <>
        (YNAHeadLineNewsRecord.tupled, YNAHeadLineNewsRecord.unapply)
  }
}

case class YNAHeadLineNewsRecord(link: String,
                                 dateTime: DateTime,
                                 title: String,
                                 summary: String,
                                 publishingDateTime: DateTime) extends TableRecords