package v1.api.repository

import com.google.inject.Inject
import play.api.db.Database
import play.db.NamedDatabase
import v1.api.execute.DataBaseExecuteContext
import v1.api.implicits.ConnectionHelper._
import v1.api.implicits.ResultSetHelper._

import javax.inject.Singleton

trait AdminRepository {
  def findAccount(username: String, password: String): Boolean

  def updateAccountPassword(username: String, oldPassword: String, newPassword: String): Int
}

@Singleton
class AdminRepositoryImpl @Inject()(@NamedDatabase("blog") database: Database)
                                   (implicit dataBaseExecuteContext: DataBaseExecuteContext) extends AdminRepository {
  override def findAccount(username: String, password: String): Boolean = {
    database.withConnection(
      conn => {
        val result = conn.prepareStatement("select * from ACCOUNT where username=? and password=?")
          .setParams(username, password)
          .executeQuery()
          .toLazyList
          .headOption
        if (result.nonEmpty)
          true
        else
          false
      }
    )
  }


  override def updateAccountPassword(username: String, oldPassword: String, newPassword: String): Int = {
    database.withConnection(
      conn => {
        val id = conn.prepareStatement("select * from ACCOUNT where username=? and password=?")
          .setParams(username, oldPassword)
          .executeQuery()
          .toLazyList
          .map(_.getInt("id"))
          .headOption
        if (id.nonEmpty) {
          conn.prepareStatement("update ACCOUNT set password=? where id=?")
            .setParams(newPassword, id)
            .executeUpdate()
        }
        else
          0
      }
    )
  }
}
