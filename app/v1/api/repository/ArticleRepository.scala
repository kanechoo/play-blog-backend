package v1.api.repository

import com.google.inject.{Inject, Singleton}
import play.api.db.Database
import play.db.NamedDatabase
import v1.api.entity.Article
import v1.api.execute.DataBaseExecuteContext
import v1.api.implicits.ArticleResultSet._
import v1.api.implicits.ConnectionUtil._
import v1.api.implicits.ResultSetHelper._
import v1.api.page.Page

import java.sql.Statement
import scala.concurrent.Future

@Singleton
class ArticleRepositoryImpl @Inject()(@NamedDatabase("blog") database: Database)
                                     (implicit dataBaseExecuteContext: DataBaseExecuteContext) extends ArticleRepository {


  override def select(id: Int): Future[Option[Article]] = {
    Future {
      database.withConnection(conn => {
        conn.preparedSql("select * from Article where id=?")
          .setParams(id)
          .executeQuery()
          .toLazyList
          .map(map2Article(_))
          .headOption
      })
    }
  }


  override def insertOne(article: Article): Future[Int] = {
    Future {
      database.withConnection(conn => {
        val ps = conn.preparedSql("insert into Article(title,author,publishTime,content,createTime) values (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)
          .setParams(article.title,
            article.author,
            article.publishTime,
            article.content,
            article.createTime)
        ps.executeUpdate()
        ps.getGeneratedIDs.head
      })
    }
  }

  override def list(page: Int, size: Int): Future[Page[Article]] = {
    Future {
      database.withConnection(conn => {
        val total: Long = conn.preparedSql("select count(*) from Article")
          .executeQuery()
          .getRowCount
        val maxSize = Math.min(10, size)
        val offset = (Math.max(1, page) - 1) * maxSize
        val items = conn.preparedSql("select * from Article limit ?,?")
          .setParams(offset, maxSize)
          .executeQuery()
          .toLazyList.map(map2Article(_))
          .toList
        Page(items, page, maxSize, total)
      })
    }
  }
}

trait ArticleRepository {
  def list(page: Int, size: Int): Future[Page[Article]]

  def select(id: Int): Future[Option[Article]]

  //def search(key: String): Future[Option[List[Article]]]

  def insertOne(article: Article): Future[Int]
}
