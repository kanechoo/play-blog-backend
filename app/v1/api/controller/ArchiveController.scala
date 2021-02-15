package v1.api.controller

import com.google.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import v1.api.action.{ArchiveBaseController, ArchiveControllerComponents, ArchiveRequest}
import v1.api.cont.JsonWrites.{defaultJsonWrites, focusArchiveWrites, pageDefaultJsonWrites, statusWrites}
import v1.api.entity.ArchiveForm

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ArchiveController @Inject()(acc: ArchiveControllerComponents)(implicit ec: ExecutionContext)
  extends ArchiveBaseController(acc) {
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
          ArchiveHandler.createArchive(archiveForm)
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
      printMessage
      ArchiveHandler.selectArchive()
        .map {
          result =>
            Ok(Json.toJson(result))
        }
  }

  def printMessage(implicit request: ArchiveRequest[AnyContent]): Unit = {
    println(s"message:${request.messagesApi.messages}")
    println(s"queryString:${request.queryString}")
    println(s"mediaType:${request.mediaType}")
    println(s"path:${request.path}")
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
}
