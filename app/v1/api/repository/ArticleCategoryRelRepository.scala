package v1.api.repository

import com.google.inject.{Inject, Singleton}
import play.api.db.Database
import play.db.NamedDatabase
import v1.api.entity.ArticleCategoryRel
import v1.api.execute.DataBaseExecuteContext
import v1.api.implicits.ConnectionUtil._
import v1.api.implicits.ResultSetHelper._

import scala.concurrent.Future

@Singleton
class ArticleCategoryRepositoryImpl @Inject()(@NamedDatabase("blog") database: Database)(implicit dataBaseExecuteContext: DataBaseExecuteContext) extends ArticleCategoryRepository {
  override def insertOne(rel: ArticleCategoryRel): Future[Option[Int]] = {
    Future {
      database.withConnection {
        conn =>
          conn.prepareStatement("select id from final table(insert ignore into article_category(article_id,category_id) values(?,?))")
            .setParams(rel.articleId, rel.categoryId)
            .executeQuery()
            .toLazyList
            .map(_.getInt(1))
            .headOption
      }
    }
  }
}

trait ArticleCategoryRepository {
  def insertOne(articleCategory: ArticleCategoryRel): Future[Option[Int]]

}