package com.example.model.persistent.table

import com.example.model.persistent.db.DBConfiguration
import org.joda.time.DateTime
import slick.lifted.ProvenShape

trait YNALastNewsLinkTable extends DBConfiguration {
  import profile.api._

  val tableName: String = "TB_YNA_LAST_NEWS_LINK"

  lazy val tableQuery: TableQuery[YNALastNewsLink] = TableQuery[YNALastNewsLink]

  protected class YNALastNewsLink(tag: Tag) extends Table[YNALastNewsLinkRecord](tag, tableName) {
    def newsUrl: Rep[String] = column[String]("news_url", O.Length(64))
    def link: Rep[String] = column[String]("link", O.Length(128))
    def updateTime: Rep[DateTime] = column[DateTime]("update_datetime")
    def pk = primaryKey(tableName + "_PK", newsUrl)
    override def * : ProvenShape[YNALastNewsLinkRecord] =
      (newsUrl, link, updateTime) <> (YNALastNewsLinkRecord.tupled, YNALastNewsLinkRecord.unapply)
  }
}

case class YNALastNewsLinkRecord(news_url: String,
                                 link: String,
                                 updateTime: DateTime) extends TableRecords
