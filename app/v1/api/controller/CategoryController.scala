package v1.api.controller

import com.google.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import v1.api.cont.JsonWrites.categoryCountJsonWrites
import v1.api.repository.CategoryRepository

import scala.concurrent.ExecutionContext

@Singleton
class CategoryController @Inject()(cc: ControllerComponents, repository: CategoryRepository)
                                  (implicit ec: ExecutionContext) extends AbstractController(cc) {
  def findCategory: Action[AnyContent] = Action.async {
    implicit request =>
      repository.findAll.map {
        result =>
          Ok(Json.toJson(result))
      }
  }
}
