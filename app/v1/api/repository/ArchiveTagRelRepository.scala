package v1.api.repository

import com.google.inject.{Inject, Singleton}
import play.api.db.Database
import play.db.NamedDatabase
import v1.api.entity.ArchiveTagRel
import v1.api.execute.DataBaseExecuteContext
import v1.api.implicits.ConnectionHelper._
import v1.api.implicits.ResultSetHelper._

import scala.concurrent.Future

@Singleton
class ArchiveTagRepositoryImpl @Inject()(@NamedDatabase("blog") database: Database)(implicit dataBaseExecuteContext: DataBaseExecuteContext) extends ArchiveTagRepository {
  override def insertOne(archiveTag: ArchiveTagRel): Future[Option[Int]] = {
    Future {
      database.withConnection {
        conn =>
          conn.prepareStatement("select id from final table(inset into archive_tag(archive_id,tag_id) values(?,?))")
            .setParams(archiveTag.archiveId, archiveTag.tagId)
            .executeQuery()
            .toLazyList
            .map(_.getInt(1))
            .headOption
      }
    }
  }

  override def batchInsert(seq: Seq[ArchiveTagRel]): Future[Array[Int]] = {
    Future {
      database.withConnection {
        conn =>
          val ps = conn.prepareStatement("insert into ARCHIVE_TAG(archive_id, tag_id) values ( ?,? )")
          seq.foreach {
            at =>
              ps.setParams(at.archiveId, at.tagId).addBatch()
          }
          ps.executeBatch()
      }
    }
  }
}

trait ArchiveTagRepository {
  def insertOne(archiveCategory: ArchiveTagRel): Future[Option[Int]]

  def batchInsert(seq: Seq[ArchiveTagRel]): Future[Array[Int]]
}
