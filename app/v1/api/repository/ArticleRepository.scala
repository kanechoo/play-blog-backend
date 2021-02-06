package v1.api.repository

import com.google.inject.{Inject, Singleton}
import play.api.db.Database
import play.db.NamedDatabase
import v1.api.entity.Article
import v1.api.execute.DataBaseExecuteContext
import v1.api.implicits.ArticleResultSet._
import v1.api.implicits.ResultSetUtil._
import v1.api.page.Page

import java.sql.Statement
import scala.concurrent.Future

@Singleton
class ArticleRepositoryImpl @Inject()(@NamedDatabase("blog") database: Database)
                                     (implicit dataBaseExecuteContext: DataBaseExecuteContext) extends ArticleRepository {


  override def select(id: Int): Future[Option[Article]] = {
    Future {
      database.withConnection(conn => {
        conn.createStatement()
          .executeQuery(Article.sql_select_by_id(id))
          .toLazyList.map(map2Article(_))
          .headOption
      })
    }
  }


  override def insert(article: Article): Future[Int] = {
    Future {
      database.withConnection(implicit connection => {
        val ps = connection.prepareStatement(Article.sql_insert(article), Statement.RETURN_GENERATED_KEYS)
        ps.executeUpdate()
        ps.getGeneratedID
      })
    }
  }

  override def list(page: Int, size: Int): Future[Page[Article]] = {
    Future {
      database.withConnection(conn => {
        val total: Long = conn.createStatement()
          .executeQuery(Article.sql_count)
          .getRowCount
        val items = conn.prepareStatement(Article.sql_select_limit(page, size))
          .executeQuery()
          .toLazyList.map(map2Article(_))
          .toList
        Page(items, page, size, total)
      })
    }
  }
}

trait ArticleRepository {
  def list(page: Int, size: Int): Future[Page[Article]]

  def select(id: Int): Future[Option[Article]]

  //def search(key: String): Future[Option[List[Article]]]

  def insert(article: Article): Future[Int]
}
