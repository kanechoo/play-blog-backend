package v1.api.repository

import com.google.inject.Inject
import play.api.db.Database
import play.db.NamedDatabase
import v1.api.entity.ArticleTagRel
import v1.api.execute.DataBaseExecuteContext
import v1.api.implicits.ConnectionUtil._
import v1.api.implicits.ResultSetHelper._

import scala.concurrent.Future

class ArticleTagRepositoryImpl @Inject()(@NamedDatabase("blog") database: Database)(implicit dataBaseExecuteContext: DataBaseExecuteContext) extends ArticleTagRepository {
  override def insertOne(articleTag: ArticleTagRel): Future[Option[Int]] = {
    Future {
      database.withConnection {
        conn =>
          conn.prepareStatement("select id from final table(inset into article_tag(article_id,tag_id) values(?,?))")
            .setParams(articleTag.articleId, articleTag.tagId)
            .executeQuery()
            .toLazyList
            .map(_.getInt(1))
            .headOption
      }
    }
  }

  override def batchInsert(seq: Seq[ArticleTagRel]): Future[Array[Int]] = {
    Future {
      database.withConnection {
        conn =>
          val ps = conn.prepareStatement("insert into ARTICLE_TAG(article_id, tag_id) values ( ?,? )")
          seq.foreach {
            at =>
              ps.setParams(at.articleId, at.tagId).addBatch()
          }
          ps.executeBatch()
      }
    }
  }
}

trait ArticleTagRepository {
  def insertOne(articleCategory: ArticleTagRel): Future[Option[Int]]

  def batchInsert(seq: Seq[ArticleTagRel]): Future[Array[Int]]
}
