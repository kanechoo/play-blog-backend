package v1.api.repository

import com.google.inject.{Inject, Singleton}
import play.api.db.Database
import play.db.NamedDatabase
import v1.api.entity.Tag
import v1.api.execute.DataBaseExecuteContext
import v1.api.implicits.ResultSetUtil._
import v1.api.implicits.TagResultSet._

import scala.concurrent.Future

trait TagRepository {
  def getById(id: Int): Future[Option[Tag]]

  def insert(tag: Tag): Future[Int]

  def batchInsert(tag: Seq[Tag]): Future[Seq[Int]]
}

@Singleton
class TagRepositoryImpl @Inject()(@NamedDatabase("blog") database: Database)(implicit dataBaseExecuteContext: DataBaseExecuteContext) extends TagRepository {
  override def getById(id: Int): Future[Option[Tag]] = {
    Future {
      database.withConnection {
        conn =>
          conn.prepareStatement(Tag.sql_select_by_id(id))
            .executeQuery()
            .toLazyList
            .map(map2Tag(_))
            .headOption
      }
    }
  }

  override def insert(tag: Tag): Future[Int] = {
    Future {
      database.withConnection {
        conn =>
          val ps = conn.prepareStatement(Tag.sql_insert(tag))
          ps.getGeneratedID
      }
    }
  }

  override def batchInsert(tag: Seq[Tag]): Future[Seq[Int]] = {
    Future {
      database.withConnection {
        conn =>
          val st = conn.createStatement()
          tag.foreach {
            t =>
              st.addBatch(Tag.sql_insert(t))
          }
          st.executeBatch().toSeq
      }
    }
  }
}
