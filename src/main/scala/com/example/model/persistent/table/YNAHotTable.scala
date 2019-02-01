package com.example.model.persistent.table

import com.example.model.persistent.db.DBConfiguration
import org.joda.time.DateTime
import slick.compiler.InsertCompiler.PrimaryKeys
import slick.lifted.ProvenShape

trait YNAHotTable extends DBConfiguration {
  import profile.api._

  val tableName: String = "TB_YNA_HOT_NEWS"

  lazy val tableQuery: TableQuery[YNAHot] = TableQuery[YNAHot]

  protected class YNAHot(tag: Tag) extends Table[YNAHotNewsRecord](tag, tableName) {
    def link: Rep[String] = column[String]("link", O.Length(128))
    def dateTime: Rep[DateTime] = column[DateTime]("datetime")
    def title: Rep[String] = column[String]("title", O.Length(256))
    def summary: Rep[String] = column[String]("summary", O.Length(256))
    def publishingDateTime: Rep[DateTime] = column[DateTime]("publishing_datetime")
    def pk = primaryKey(tableName + "_PK", link)
    override def * : ProvenShape[YNAHotNewsRecord] =
      (link, dateTime, title, summary, publishingDateTime) <>
        (YNAHotNewsRecord.tupled, YNAHotNewsRecord.unapply)
  }
}

case class YNAHotNewsRecord(link: String,
                            dateTime: DateTime,
                            title: String,
                            summary: String,
                            publishingDateTime: DateTime) extends TableRecords