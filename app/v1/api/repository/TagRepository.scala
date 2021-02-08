package v1.api.repository

import com.google.inject.{Inject, Singleton}
import play.api.db.Database
import play.db.NamedDatabase
import v1.api.entity.Tag
import v1.api.execute.DataBaseExecuteContext
import v1.api.implicits.ConnectionHelper._
import v1.api.implicits.PreparedStatementPlaceHolder._
import v1.api.implicits.ResultSetHelper._

import scala.concurrent.Future

trait TagRepository {
  def getById(id: Int): Future[Option[Tag]]

  def insertOne(tag: Tag): Future[Option[Int]]

  def batchInsert(tag: Seq[Tag]): Future[Seq[Int]]
}

@Singleton
class TagRepositoryImpl @Inject()(@NamedDatabase("blog") database: Database)(implicit dataBaseExecuteContext: DataBaseExecuteContext) extends TagRepository {
  override def getById(id: Int): Future[Option[Tag]] = {
    Future {
      database.withConnection {
        conn =>
          conn.prepareStatement("select * from tag where id=?")
            .setParams(id)
            .executeQuery()
            .toLazyList
            .map(_.asTag)
            .headOption
      }
    }
  }

  override def insertOne(tag: Tag): Future[Option[Int]] = {
    Future {
      database.withConnection {
        conn =>
          conn.prepareStatement("select id from final table(insert ignore into tag(tag) values(?))")
            .setParams(tag.tag)
            .executeQuery()
            .toLazyList
            .map(_.getInt(1))
            .headOption
      }
    }
  }

  override def batchInsert(tag: Seq[Tag]): Future[Seq[Int]] = {
    Future {
      database.withConnection {
        conn =>
          val ps = conn.prepareStatement("insert ignore into tag(tag) values(?)")
          tag.foreach {
            t =>
              ps.setParams(t.tag).addBatch()
          }
          ps.executeBatch()
          conn.prepareStatement(s"select * from TAG where tag in(${tag.length.params})")
            .setInParams(tag.map(_.tag))
            .executeQuery()
            .toLazyList
            .map(_.getInt("id"))
            .toList
      }
    }
  }
}
