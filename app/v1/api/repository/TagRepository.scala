package v1.api.repository

import com.google.inject.{Inject, Singleton}
import play.api.db.Database
import play.db.NamedDatabase
import v1.api.entity.Tag
import v1.api.execute.DataBaseExecuteContext
import v1.api.implicits.ConnectionUtil._
import v1.api.implicits.ResultSetHelper._
import v1.api.implicits.TagResultSet._

import java.sql.Statement
import scala.concurrent.Future

trait TagRepository {
  def getById(id: Int): Future[Option[Tag]]

  def insertOne(tag: Tag): Future[Int]

  def batchInsert(tag: Seq[Tag]): Future[Seq[Int]]
}

@Singleton
class TagRepositoryImpl @Inject()(@NamedDatabase("blog") database: Database)(implicit dataBaseExecuteContext: DataBaseExecuteContext) extends TagRepository {
  override def getById(id: Int): Future[Option[Tag]] = {
    Future {
      database.withConnection {
        conn =>
          conn.preparedSql("select * from tag where id=?")
            .setParams(id)
            .executeQuery()
            .toLazyList
            .map(map2Tag(_))
            .headOption
      }
    }
  }

  override def insertOne(tag: Tag): Future[Int] = {
    Future {
      database.withConnection {
        conn =>
          val ps = conn.preparedSql("insert into tag(tag) values(?)")
            .setParams(tag.tag)
          ps.executeQuery()
          ps.getGeneratedIDs.head
      }
    }
  }

  override def batchInsert(tag: Seq[Tag]): Future[Seq[Int]] = {
    Future {
      database.withConnection {
        conn =>
          val ps = conn.preparedSql("insert into tag(tag) values(?)", Statement.RETURN_GENERATED_KEYS)
          tag.foreach {
            t =>
              ps.setParams(t.tag).addBatch()
          }
          ps.executeBatch()
          ps.getGeneratedIDs
      }
    }
  }
}
