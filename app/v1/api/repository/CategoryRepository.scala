package v1.api.repository

import com.google.inject.{Inject, Singleton}
import play.api.Logger
import play.api.db.Database
import play.db.NamedDatabase
import v1.api.entity.{Category, CategoryCount}
import v1.api.execute.DataBaseExecuteContext
import v1.api.implicits.ConnectionHelper._
import v1.api.implicits.PreparedStatementPlaceHolder._
import v1.api.implicits.ResultSetHelper._

import scala.concurrent.Future


trait CategoryRepository {
  def getById(id: Int): Future[Option[Category]]

  def insertOne(category: Category): Future[Option[Int]]

  /**
    * Batch insert category and return ids
    *
    * @param category entity
    * @return ids in table
    */
  def batchInsert(category: Seq[Category]): Future[Seq[Int]]

  def findAll: Future[Seq[CategoryCount]]
}

@Singleton
class CategoryRepositoryImpl @Inject()(@NamedDatabase("blog") database: Database)(implicit dataBaseExecuteContext: DataBaseExecuteContext) extends CategoryRepository {
  val log: Logger = Logger(getClass)

  override def getById(id: Int): Future[Option[Category]] = {
    Future {
      database.withConnection {
        conn =>
          conn.prepareStatement("select * from category where id=?")
            .setParams(id)
            .executeQuery()
            .toLazyList
            .map(_.asCategory)
            .headOption
      }
    }
  }

  override def insertOne(category: Category): Future[Option[Int]] = {
    Future {
      database.withConnection {
        conn =>
          conn.prepareStatement("select id from final table(insert ignore into category(category) values(?))")
            .setParams(category.category)
            .executeQuery()
            .toLazyList
            .map(_.getInt(1))
            .headOption
      }
    }
  }

  override def batchInsert(category: Seq[Category]): Future[Seq[Int]] = {
    Future {
      database.withConnection {
        conn =>
          val ps = conn.prepareStatement("insert ignore into category(category) values (?)")
          category.foreach {
            c =>
              ps.setParams(c.category)
                .addBatch()
          }
          ps.executeBatch()
          conn.prepareStatement(s"select * from category where category in (${category.size.params})")
            .setInParams(category.map(_.category))
            .executeQuery()
            .toLazyList
            .map(_.getInt("id"))
            .toList
      }
    }

  }

  override def findAll: Future[Seq[CategoryCount]] = Future {
    database.withConnection {
      conn =>
        conn.prepareStatement("SELECT C.CATEGORY AS category,COUNT(*) AS count FROM CATEGORY C LEFT JOIN ARCHIVE_CATEGORY AC ON AC.CATEGORY_ID = C.ID GROUP BY C.CATEGORY ORDER BY count DESC")
          .executeQuery()
          .toLazyList
          .map {
            resultSet =>
              CategoryCount(resultSet.getString("category"), resultSet.getInt("count"))
          }
          .toList
    }
  }
}
