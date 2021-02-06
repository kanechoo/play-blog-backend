package v1.api.repository

import com.google.inject.{Inject, Singleton}
import play.api.db.Database
import play.db.NamedDatabase
import v1.api.entity.ArticleCategoryRel
import v1.api.execute.DataBaseExecuteContext
import v1.api.implicits.ConnectionUtil._
import v1.api.implicits.ResultSetHelper._

import java.sql.Statement
import scala.concurrent.Future

@Singleton
class ArticleCategoryRepositoryImpl @Inject()(@NamedDatabase("blog") database: Database)(implicit dataBaseExecuteContext: DataBaseExecuteContext) extends ArticleCategoryRepository {
  override def insertOne(rel: ArticleCategoryRel): Future[Int] = {
    Future {
      database.withConnection {
        conn =>
          val ps = conn.preparedSql("insert into article_category(article_id,category_id) values(?,?)", Statement.RETURN_GENERATED_KEYS)
            .setParams(rel.articleId, rel.categoryId)
          ps.executeQuery()
          ps.getGeneratedIDs.head
      }
    }
  }
}

trait ArticleCategoryRepository {
  def insertOne(articleCategory: ArticleCategoryRel): Future[Int]

}