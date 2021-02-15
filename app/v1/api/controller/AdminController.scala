package v1.api.controller

import com.google.inject.Inject
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class AdminController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  def signIn: Action[AnyContent] = Action.async {
    implicit request =>
      Future {
        Ok("message")
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
