package v1.api.repository

import com.google.inject.{Inject, Singleton}
import play.api.db.Database
import play.db.NamedDatabase
import v1.api.cont.ArticleSql._
import v1.api.entity.Article
import v1.api.execute.DataBaseExecuteContext
import v1.api.implicits.ConnectionUtil._
import v1.api.implicits.ResultSetHelper._
import v1.api.page.Page

import scala.concurrent.Future

@Singleton
class ArticleRepositoryImpl @Inject()(@NamedDatabase("blog") database: Database)
                                     (implicit dataBaseExecuteContext: DataBaseExecuteContext) extends ArticleRepository {


  override def selectById(id: Int): Future[Option[Article]] = {
    Future {
      database.withConnection(conn => {
        conn.prepareStatement(selectSql + " where `article`.id=?")
          .setParams(id)
          .executeQuery()
          .toLazyList
          .map(_.asArticle)
          .headOption
      })
    }
  }


  override def insertOne(article: Article): Future[Option[Int]] = {
    Future {
      database.withConnection {
        conn => {
          conn.prepareStatement("select id from final table (insert into Article(title,author,publishTime,content,createTime) values (?,?,?,?,?))")
            .setParams(article.title,
              article.author,
              article.publishTime,
              article.content,
              article.createTime)
            .executeQuery()
            .toLazyList
            .map(_.getInt(1))
            .toList
            .headOption
        }
      }
    }
  }

  override def list(page: Int, size: Int): Future[Page[Article]] = {
    Future {
      database.withConnection(conn => {
        val total: Long = conn.prepareStatement("select count(*) from Article")
          .executeQuery()
          .getRowCount
        val maxSize = Math.min(10, size)
        val offset = (Math.max(1, page) - 1) * maxSize
        val items = conn.prepareStatement(selectSql + " limit ? offset ?")
          .setParams(maxSize, offset)
          .executeQuery()
          .toLazyList.map(_.asArticle)
          .toList
        Page(items, page, maxSize, total)
      })
    }
  }
}

trait ArticleRepository {
  def list(page: Int, size: Int): Future[Page[Article]]

  def selectById(id: Int): Future[Option[Article]]

  //def search(key: String): Future[Option[List[Article]]]

  def insertOne(article: Article): Future[Option[Int]]
}
