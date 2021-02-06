package v1.api.repository

import com.google.inject.{Inject, Singleton}
import play.api.db.Database
import play.db.NamedDatabase
import v1.api.entity.Category
import v1.api.execute.DataBaseExecuteContext
import v1.api.implicits.CategoryResultSet._
import v1.api.implicits.ConnectionUtil._
import v1.api.implicits.ResultSetHelper._

import java.sql.Statement
import scala.concurrent.Future

trait CategoryRepository {
  def getById(id: Int): Future[Option[Category]]

  def insertOne(category: Category): Future[Int]

  def batchInsert(category: Seq[Category]): Future[Seq[Int]]
}

@Singleton
class CategoryRepositoryImpl @Inject()(@NamedDatabase("blog") database: Database)(implicit dataBaseExecuteContext: DataBaseExecuteContext) extends CategoryRepository {


  override def getById(id: Int): Future[Option[Category]] = {
    Future {
      database.withConnection {
        conn =>
          conn.preparedSql("select * from category where id=?")
            .setParams(id)
            .executeQuery()
            .toLazyList
            .map(map2Category(_))
            .headOption
      }
    }
  }

  override def insertOne(category: Category): Future[Int] = {
    Future {
      database.withConnection {
        conn =>
          val ps = conn.preparedSql("insert into category(category) values(?)", Statement.RETURN_GENERATED_KEYS)
            .setParams(category.category)
          ps.executeQuery()
          ps.getGeneratedIDs.head
      }
    }
  }

  override def batchInsert(category: Seq[Category]): Future[Seq[Int]] = {
    Future {
      database.withConnection {
        conn =>
          val ps = conn.preparedSql("insert into category(category) values (?)", Statement.RETURN_GENERATED_KEYS)
          category.foreach {
            c =>
              ps.setParams(c.category)
                .addBatch()
          }
          ps.executeBatch()
          ps.getGeneratedIDs
      }
    }
  }
}
