package v1.api.repository

import com.google.inject.Inject
import play.api.db.Database
import play.db.NamedDatabase
import v1.api.entity.{Account, ResponseMessage, SerialNumber}
import v1.api.execute.DataBaseExecuteContext
import v1.api.implicits.ConnectionHelper._
import v1.api.implicits.ResultSetHelper._

import javax.inject.Singleton
import scala.concurrent.Future

trait AdminRepository {
  def findAccount(username: String, password: String): Future[ResponseMessage]

  def updateAccountPassword(username: String, oldPassword: String, newPassword: String): Future[ResponseMessage]
}

@Singleton
class AdminRepositoryImpl @Inject()(@NamedDatabase("blog") database: Database)
                                   (implicit dataBaseExecuteContext: DataBaseExecuteContext) extends AdminRepository {
  override def findAccount(username: String, password: String): Future[ResponseMessage] = Future {
    database.withConnection(
      conn => {
        val account = conn.prepareStatement("select * from ACCOUNT where username=? ")
          .setParams(username)
          .executeQuery()
          .toLazyList
          .map {
            res =>
              Account(SerialNumber(res.getInt("id")),
                res.getString("username"),
                res.getString("password"))
          }
          .headOption
        if (account.nonEmpty) {
          if (account.head.password.equals(password)) {
            ResponseMessage(1, "Sign in success")
          }
          else {
            ResponseMessage(0, "Wrong password")
          }
        }
        else ResponseMessage(0, "Wrong username or password")
      }
    )
  }


  override def updateAccountPassword(username: String, oldPassword: String, newPassword: String): Future[ResponseMessage] = Future {
    database.withConnection(
      conn => {
        val id = conn.prepareStatement("select * from ACCOUNT where username=? and password=?")
          .setParams(username, oldPassword)
          .executeQuery()
          .toLazyList
          .map(_.getInt("id"))
          .headOption
        if (id.nonEmpty) {
          val status = conn.prepareStatement("update ACCOUNT set password=? where id=?")
            .setParams(newPassword, id)
            .executeUpdate()
          ResponseMessage(status, "Update password success.")
        }
        else
          ResponseMessage(0, "Update password failed.")
      }
    )
  }
}
