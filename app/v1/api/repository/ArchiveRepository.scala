package v1.api.repository

import com.google.inject.{Inject, Singleton}
import play.api.db.Database
import play.api.mvc.AnyContent
import play.db.NamedDatabase
import v1.api.action.ArchiveRequest
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
        conn.prepareStatement(selectByIdSql)
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
          conn.prepareStatement("""select id from final table (insert ignore into Archive(title,author,publishTime,content,createTime) values (?,?,?,?,?))""")
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

  override def list()(implicit request: ArchiveRequest[AnyContent]): Future[Page[Archive]] = {
    Future {
      database.withConnection(conn => {
        val total: Long = conn.prepareStatement("select count(*) from ARCHIVE")
          .executeQuery()
          .getRowCount
        val params = request.archiveQueryParams
        val items = conn.prepareStatement(selectSql)
          .setParams(params.limit, params.offset)
          .executeQuery()
          .toLazyList.map(_.asArchive)
          .toList
        val page = (params.offset / params.limit) + 1
        Page(items, page, params.limit, total)
      })
    }
  }

  override def selectByCategoryName(categoryName: String)(implicit request: ArchiveRequest[AnyContent]): Future[Page[Archive]] = {
    Future {
      database.withConnection {
        conn =>
          val params = request.archiveQueryParams
          val total = conn.prepareStatement(selectCountByCategoryNameSql)
            .setParams(categoryName)
            .executeQuery()
            .getRowCount
          val items = conn.prepareStatement(selectByCategoryNameSql)
            .setParams(categoryName,
              params.limit,
              params.offset)
            .executeQuery()
            .toLazyList
            .map(_.asArchive)
            .toList
          val page = (params.offset / params.limit) + 1
          Page(items, page, params.limit, total)
      }
    }
  }

  override def selectByTagName(tagName: String): Future[Page[Archive]] = {
    null
  }
}

trait ArchiveRepository {
  def list()(implicit request: ArchiveRequest[AnyContent]): Future[Page[Archive]]

  def selectById(id: Int): Future[Option[Archive]]

  //def search(key: String): Future[Option[List[Archive]]]

  def insertOne(archive: Archive): Future[Option[Int]]

  def selectByCategoryName(categoryName: String)(implicit request: ArchiveRequest[AnyContent]): Future[Page[Archive]]

  def selectByTagName(tagName: String): Future[Page[Archive]]
}
