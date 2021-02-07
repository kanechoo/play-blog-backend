package v1.api.repository

import com.google.inject.{Inject, Singleton}
import play.api.db.Database
import play.db.NamedDatabase
import v1.api.entity.ArchiveCategoryRel
import v1.api.execute.DataBaseExecuteContext
import v1.api.implicits.ConnectionUtil._
import v1.api.implicits.ResultSetHelper._

import scala.concurrent.Future

@Singleton
class ArchiveCategoryRepositoryImpl @Inject()(@NamedDatabase("blog") database: Database)(implicit dataBaseExecuteContext: DataBaseExecuteContext) extends ArchiveCategoryRepository {
  override def insertOne(rel: ArchiveCategoryRel): Future[Option[Int]] = {
    Future {
      database.withConnection {
        conn =>
          conn.prepareStatement("select id from final table(insert ignore into archive_category(archive_id,category_id) values(?,?))")
            .setParams(rel.archiveId, rel.categoryId)
            .executeQuery()
            .toLazyList
            .map(_.getInt(1))
            .headOption
      }
    }
  }

  override def batchInsert(seq: Seq[ArchiveCategoryRel]): Future[Array[Int]] = {
    Future {
      database.withConnection {
        conn =>
          val ps = conn.prepareStatement("insert into ARCHIVE_CATEGORY(archive_id, category_id) values ( ?,? )")
          seq.foreach {
            ac =>
              ps.setParams(ac.archiveId, ac.categoryId).addBatch()
          }
          ps.executeBatch()
      }
    }
  }
}

trait ArchiveCategoryRepository {
  def insertOne(archiveCategory: ArchiveCategoryRel): Future[Option[Int]]

  def batchInsert(seq: Seq[ArchiveCategoryRel]): Future[Array[Int]]
}