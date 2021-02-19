package v1.api.controller

import com.google.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import v1.api.cont.JsonWrites
import v1.api.repository.TagRepository

import scala.concurrent.ExecutionContext

@Singleton
class TagController @Inject()(cc: ControllerComponents, repository: TagRepository)(implicit ec: ExecutionContext) extends AbstractController(cc) with JsonWrites {
  def findTag: Action[AnyContent] = Action.async {
    implicit request =>
      repository.findAll.map {
        result =>
          Ok(Json.toJson(result))
      }
  }
}

