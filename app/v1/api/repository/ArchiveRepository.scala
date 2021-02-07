package v1.api.repository

import com.google.inject.{Inject, Singleton}
import play.api.db.Database
import play.db.NamedDatabase
import v1.api.cont.ArchiveSql._
import v1.api.entity.Archive
import v1.api.execute.DataBaseExecuteContext
import v1.api.implicits.ConnectionUtil._
import v1.api.implicits.ResultSetHelper._
import v1.api.page.Page

import scala.concurrent.Future

@Singleton
class ArchiveRepositoryImpl @Inject()(@NamedDatabase("blog") database: Database)
                                     (implicit dataBaseExecuteContext: DataBaseExecuteContext) extends ArchiveRepository {


  override def selectById(id: Int): Future[Option[Archive]] = {
    Future {
      database.withConnection(conn => {
        conn.prepareStatement(selectSql + " where `archive`.id=?")
          .setParams(id)
          .executeQuery()
          .toLazyList
          .map(_.asArchive)
          .headOption
      })
    }
  }


  override def insertOne(archive: Archive): Future[Option[Int]] = {
    Future {
      database.withConnection {
        conn => {
          conn.prepareStatement("select id from final table (insert into Archive(title,author,publishTime,content,createTime) values (?,?,?,?,?))")
            .setParams(archive.title,
              archive.author,
              archive.publishTime,
              archive.content,
              archive.createTime)
            .executeQuery()
            .toLazyList
            .map(_.getInt(1))
            .toList
            .headOption
        }
      }
    }
  }

  override def list(page: Int, size: Int): Future[Page[Archive]] = {
    Future {
      database.withConnection(conn => {
        val total: Long = conn.prepareStatement("select count(*) from Archive")
          .executeQuery()
          .getRowCount
        val maxSize = Math.min(10, size)
        val offset = (Math.max(1, page) - 1) * maxSize
        val items = conn.prepareStatement(selectSql + " limit ? offset ?")
          .setParams(maxSize, offset)
          .executeQuery()
          .toLazyList.map(_.asArchive)
          .toList
        Page(items, page, maxSize, total)
      })
    }
  }

  override def selectByCategory(categoryId: Int): Future[Page[Archive]] = {
    null
  }

  override def selectByTag(tagId: Int): Future[Page[Archive]] = {
    null
  }
}

trait ArchiveRepository {
  def list(page: Int, size: Int): Future[Page[Archive]]

  def selectById(id: Int): Future[Option[Archive]]

  //def search(key: String): Future[Option[List[Archive]]]

  def insertOne(archive: Archive): Future[Option[Int]]

  def selectByCategory(categoryId: Int): Future[Page[Archive]]

  def selectByTag(tagId: Int): Future[Page[Archive]]
}
