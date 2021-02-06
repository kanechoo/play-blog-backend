package v1.api.repository

import com.google.inject.Inject
import play.api.db.Database
import play.db.NamedDatabase
import v1.api.entity.ArticleTagRel
import v1.api.execute.DataBaseExecuteContext
import v1.api.implicits.ConnectionUtil._
import v1.api.implicits.ResultSetHelper._

import java.sql.Statement
import scala.concurrent.Future

class ArticleTagRepositoryImpl @Inject()(@NamedDatabase("blog") database: Database)(implicit dataBaseExecuteContext: DataBaseExecuteContext) extends ArticleTagRepository {
  override def insertOne(articleTag: ArticleTagRel): Future[Int] = {
    Future {
      database.withConnection {
        conn =>
          val ps = conn.preparedSql("inset into article_tag(article_id,tag_id) values(?,?)", Statement.RETURN_GENERATED_KEYS)
            .setParams(articleTag.articleId, articleTag.tagId)
          ps.executeQuery()
          ps.getGeneratedIDs.head
      }
    }
  }
}

trait ArticleTagRepository {
  def insertOne(articleCategory: ArticleTagRel): Future[Int]
}
