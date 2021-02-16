package v1.api.repository

import com.google.inject.{Inject, Singleton}
import play.api.Logger
import play.api.db.Database
import play.db.NamedDatabase
import v1.api.entity.{Tag, TagCount}
import v1.api.execute.DataBaseExecuteContext
import v1.api.implicits.ConnectionHelper._
import v1.api.implicits.ResultSetHelper._

import java.sql.Connection
import scala.concurrent.Future

trait TagRepository {
  def getById(id: Int): Future[Option[Tag]]

  def insertOne(tag: Tag)(implicit conn: Connection): Option[Int]

  def batchInsert(tag: Seq[Tag]): Future[Seq[Int]]

  def findAll: Future[Seq[TagCount]]
}

@Singleton
class TagRepositoryImpl @Inject()(@NamedDatabase("blog") database: Database)(implicit dataBaseExecuteContext: DataBaseExecuteContext) extends TagRepository {
  val log: Logger = Logger(getClass)

  override def getById(id: Int): Future[Option[Tag]] = {
    Future {
      database.withConnection {
        conn =>
          conn.prepareStatement("select * from tag where id=?", Array("id"))
            .setParams(id)
            .executeQuery()
            .toLazyList
            .map(_.asTag)
            .headOption
      }
    }
  }

  override def batchInsert(tag: Seq[Tag]): Future[Seq[Int]] = {
    Future {
      this.synchronized {
        implicit val conn: Connection = database.getConnection()
        val ids = tag.map {
          t =>
            insertOne(t).head
        }
        conn.close()
        ids
      }
    }
  }

  override def insertOne(tag: Tag)(implicit conn: Connection): Option[Int] = {
    val checkExists = conn.prepareStatement("select id from TAG where TAG.TAG=?")
      .setParams(tag.tag)
      .executeQuery()
      .toLazyList
      .map(_.getInt("id"))
      .headOption
    if (checkExists.nonEmpty) {
      checkExists
    }
    else {
      log.debug(s"${dataBaseExecuteContext}")
      log.debug(s"tag : ${tag.tag} is not exists.")
      val ps = conn.prepareStatement("insert into tag(tag) values(?)")
        .setParams(tag.tag)
      ps.executeUpdate()
      ps.getGeneratedKeys
        .toLazyList
        .map(_.getInt(1))
        .headOption
    }

  }

  override def findAll: Future[Seq[TagCount]] = Future {
    database.withConnection {
      conn =>
        conn.prepareStatement("SELECT TAG.TAG as tag,COUNT(*) as count FROM TAG LEFT JOIN ARCHIVE_TAG ON TAG.ID = ARCHIVE_TAG.TAG_ID GROUP BY TAG.TAG ORDER BY count DESC")
          .executeQuery()
          .toLazyList
          .map {
            resultSet =>
              TagCount(resultSet.getString("tag"), resultSet.getInt("count"))
          }
          .toList
    }
  }
}
