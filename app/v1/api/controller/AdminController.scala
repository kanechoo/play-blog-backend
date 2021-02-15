package v1.api.controller

import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import v1.api.cont.JsonWrites.statusWrites
import v1.api.entity.ResponseMessage
import v1.api.repository.AdminRepository

import scala.concurrent.{ExecutionContext, Future}

class AdminController @Inject()(cc: ControllerComponents, adminRepository: AdminRepository)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  def signIn: Action[AnyContent] = Action.async {
    implicit request =>
      val data = request.body.asMultipartFormData
      if (data.isEmpty) Future {
        Ok(Json.toJson(ResponseMessage(0, "Wrong data format")))
      }
      else {
        val username = data.head.asFormUrlEncoded.get("username")
        val password = data.head.asFormUrlEncoded.get("password")
        if ((null == username || username.isEmpty) || (null == password || password.isEmpty)) Future {
          Ok(Json.toJson(ResponseMessage(0, "Username and password not allow empty")))
        }
        else {
          adminRepository.findAccount(username.head.head, password.head.head)
            .map {
              res =>
                if (1 == res.status) {
                  Ok(Json.toJson(res)).withSession(request.session + ("username" -> username.head.head))
                }
                else
                  Ok(Json.toJson(res))
            }
        }
      }
  }

  def signOut: Action[AnyContent] = Action.async {
    implicit request =>
      Future {
        Ok("message")
      }
  }

  def updatePassword(oldPassword: String, newPassword: String): Action[AnyContent] = Action.async {
    implicit request =>
      Future {
        Ok("message")
      }
  }
}
