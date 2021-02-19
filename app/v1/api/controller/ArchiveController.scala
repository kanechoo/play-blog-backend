package v1.api.controller

import com.google.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import v1.api.action.{ArchiveBaseController, ArchiveControllerComponents}
import v1.api.cont.JsonWrites
import v1.api.entity.ArchiveForm

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ArchiveController @Inject()(acc: ArchiveControllerComponents)(implicit ec: ExecutionContext)
  extends ArchiveBaseController(acc) with JsonWrites {
  private val form: Form[ArchiveForm] = {
    import play.api.data.Forms._
    Form(
      mapping(
        "title" -> nonEmptyText,
        "author" -> nonEmptyText,
        "publishTime" -> date("yyyy-MM-dd HH:mm:ss"),
        "content" -> nonEmptyText,
        "category" -> text,
        "tag" -> text
      )(ArchiveForm.customApply)(ArchiveForm.customUnApply)
    )
  }

  def createNewArchive: Action[AnyContent] = ArchiveAction.async { implicit request => {
    form.bindFromRequest()
      .fold(
        (badForm: Form[ArchiveForm]) => {
          Future {
            BadRequest(badForm.errorsAsJson)
          }
        },
        (archiveForm: ArchiveForm) => {
          ArchiveHandler.createArchive(archiveForm.getArchiveFormData)
            .map {
              archive =>
                Created(Json.toJson(archive))
            }
        }
      )
  }
  }

  def findById(id: Int): Action[AnyContent] = ArchiveAction.async {
    implicit request =>
      ArchiveHandler.selectById(id)
        .map {
          archive =>
            Ok(Json.toJson(archive))
        }
  }

  def findArchives: Action[AnyContent] = ArchiveAction.async {
    implicit request =>
      ArchiveHandler.selectArchive(request.archiveQueryParams)
        .map {
          result =>
            Ok(Json.toJson(result))
        }
  }

  def findByCategoryName(categoryName: String): Action[AnyContent] = ArchiveAction.async {
    implicit request =>
      ArchiveHandler.selectByCategoryName(categoryName)
        .map {
          result =>
            Ok(Json.toJson(result))
        }
  }

  def findByTagName(tagName: String): Action[AnyContent] = ArchiveAction.async {
    implicit request =>
      ArchiveHandler.selectByTagName(tagName)
        .map {
          result =>
            Ok(Json.toJson(result))
        }
  }

  def deletePostById(id: Int): Action[AnyContent] = ArchiveAction.async {
    implicit request =>
      ArchiveHandler.deletePostById(id)
        .map(status =>
          Ok(Json.toJson(status))
        )
  }

  def timeline: Action[AnyContent] = ArchiveAction.async {
    implicit request =>
      ArchiveHandler.timeline(request.archiveQueryParams)
        .map {
          result =>
            Ok(Json.toJson(result))
        }
  }
}
