package v1.api.repository

import com.google.inject.{Inject, Singleton}
import play.api.db.Database
import play.api.mvc.AnyContent
import play.db.NamedDatabase
import v1.api.action.ArchiveRequest
import v1.api.cont.ArchiveSql._
import v1.api.entity._
import v1.api.execute.DataBaseExecuteContext
import v1.api.implicits.ConnectionHelper._
import v1.api.implicits.ResultSetHelper._
import v1.api.page.Page

import java.text.SimpleDateFormat
import scala.concurrent.Future

@Singleton
class ArchiveRepositoryImpl @Inject()(@NamedDatabase("blog") database: Database)
                                     (implicit dataBaseExecuteContext: DataBaseExecuteContext) extends ArchiveRepository {


  override def selectById(id: Int): Future[Option[FocusArchive]] = Future {
    database.withConnection(conn => {
      val archive = conn.prepareStatement(selectByIdSql)
        .setParams(id)
        .executeQuery()
        .toLazyList
        .map(_.asArchive)
        .headOption
      if (archive.nonEmpty) {
        val next = conn.prepareStatement("select id,title from ARCHIVE where PUBLISHTIME < ? order by PUBLISHTIME desc limit 1")
          .setParams(archive.head.publishTime)
          .executeQuery()
          .toLazyList
          .map(res => NextArchive(SerialNumber(res.getInt("id")), res.getString("title")))
          .headOption
        val previous = conn.prepareStatement("select id,title from ARCHIVE where PUBLISHTIME > ? order by PUBLISHTIME asc limit 1")
          .setParams(archive.head.publishTime)
          .executeQuery()
          .toLazyList
          .map(res => PreviousArchive(SerialNumber(res.getInt("id")), res.getString("title")))
          .headOption
        val a = archive.get
        Some(FocusArchive(a.serialNumber, a.title, a.author, a.publishTime, a.content, a.createTime, a.category, a.tag, a.catalog, previous.orNull, next.orNull))
      }
      else {
        None
      }
    })
  }


  override def insertOne(archive: Archive): Future[Option[Int]] = Future {
    database.withConnection {
      conn => {
        val checkExists = conn.prepareStatement("select id from ARCHIVE where ARCHIVE.TITLE=?")
          .setParams(archive.title)
          .executeQuery()
          .toLazyList
          .toList
          .headOption
        if (checkExists.nonEmpty || checkExists.isDefined) {
          None
        }
        else {
          val ps = conn.prepareStatement("insert into Archive(title,author,publishTime,content,createTime,catalog) values (?,?,?,?,?,?)", Array("id"))
            .setParams(archive.title,
              archive.author,
              archive.publishTime,
              archive.content,
              archive.createTime,
              archive.catalog)
          ps.executeUpdate()
          ps.getGeneratedKeys
            .toLazyList
            .map(_.getInt(1))
            .headOption
        }
      }
    }
  }

  override def list(params: ArchiveQueryParams): Future[Page[Archive]] = Future {
    database.withConnection(conn => {
      val total: Long = conn.prepareStatement("select count(*) from ARCHIVE")
        .executeQuery()
        .getRowCount
        .getOrElse(0L)
      val items = conn.prepareStatement(selectSql)
        .setParams(params.limit, params.offset)
        .executeQuery()
        .toLazyList.map(_.asArchive)
        .toList
      Page(items, params.page, params.limit, total, countTotalPage(total, params.limit))
    })
  }

  override def deleteById(id: Int): Future[ResponseMessage] = Future {
    database.withConnection(
      conn => {
        val status = conn.prepareStatement("delete from ARCHIVE where ID=?")
          .setParams(id)
          .executeUpdate()
        if (status == 1) {
          ResponseMessage(status, "delete post success")
        }
        else
          ResponseMessage(status, "delete post fail")
      }
    )
  }

  override def selectByCategory(categoryName: String)(implicit request: ArchiveRequest[AnyContent]): Future[Page[Archive]] = Future {
    database.withConnection {
      conn =>
        import request.archiveQueryParams._
        val total = conn.prepareStatement(selectCountByCategoryNameSql)
          .setParams(categoryName)
          .executeQuery()
          .getRowCount
          .getOrElse(0L)
        val items = conn.prepareStatement(selectByCategoryNameSql)
          .setParams(categoryName,
            limit,
            offset)
          .executeQuery()
          .toLazyList
          .map(_.asArchive)
          .toList
        Page(items, page, limit, total, countTotalPage(total, limit))
    }
  }

  override def selectByTag(tagName: String)(implicit request: ArchiveRequest[AnyContent]): Future[Page[Archive]] = Future {
    database.withConnection {
      conn =>
        import request.archiveQueryParams._
        val total = conn.prepareStatement(selectCountByTagNameSql)
          .setParams(tagName)
          .executeQuery()
          .getRowCount
          .getOrElse(0L)
        val items = conn.prepareStatement(selectByTagNameSql)
          .setParams(tagName,
            limit,
            offset)
          .executeQuery()
          .toLazyList
          .map(_.asArchive)
          .toList
        Page(items, page, limit, total, countTotalPage(total, limit))
    }
  }

  def countTotalPage(total: Long, limit: Int): Int = {
    (total.toInt / limit) + (if (total.toInt % limit == 0) 0 else 1)
  }

  override def timeline(params: ArchiveQueryParams): Future[Page[Timeline]] = Future {
    database.withConnection(conn => {
      val sdf = new SimpleDateFormat("yyyy/MM/dd")
      val total: Long = conn.prepareStatement("select count(*) from ARCHIVE")
        .executeQuery()
        .getRowCount
        .getOrElse(0L)
      val items = conn.prepareStatement(selectSql)
        .setParams(params.limit, params.offset)
        .executeQuery()
        .toLazyList.map(_.asArchive)
        .map {
          a =>
            Timeline(a.serialNumber, a.title, a.publishTime.getTime, a.category, a.tag)
        }
        .toList
      Page(items, params.page, params.limit, total, countTotalPage(total, params.limit))
    })
  }
}

trait ArchiveRepository {
  def list(params: ArchiveQueryParams): Future[Page[Archive]]

  def selectById(id: Int): Future[Option[FocusArchive]]

  //def search(key: String): Future[Option[List[Archive]]]
  def deleteById(id: Int): Future[ResponseMessage]

  def insertOne(archive: Archive): Future[Option[Int]]

  def selectByCategory(categoryName: String)(implicit request: ArchiveRequest[AnyContent]): Future[Page[Archive]]

  def selectByTag(tagName: String)(implicit request: ArchiveRequest[AnyContent]): Future[Page[Archive]]

  def timeline(params: ArchiveQueryParams): Future[Page[Timeline]]
}
