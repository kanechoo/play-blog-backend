package v1.api.controller

import com.google.inject.Inject
import play.api.data.Form
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import v1.api.action.{ArticleBaseController, ArticleControllerComponents, ArticleRequest}
import v1.api.cont.Const._
import v1.api.entity.ArticleForm

import scala.concurrent.{ExecutionContext, Future}

class ArticleController @Inject()(acc: ArticleControllerComponents)(implicit ec: ExecutionContext)
  extends ArticleBaseController(acc) {
  private val form: Form[ArticleForm] = {
    import play.api.data.Forms._
    Form(
      mapping(
        "title" -> nonEmptyText,
        "author" -> nonEmptyText,
        "publishTime" -> date,
        "content" -> nonEmptyText,
        "category" -> text,
        "tag" -> text
      )(ArticleForm.customApply)(ArticleForm.customUnApply)
    )
  }

  def createNewArticle: Action[AnyContent] = ArticleAction.async { implicit request => {
    form.bindFromRequest()
      .fold(
        (badForm: Form[ArticleForm]) => {
          Future {
            BadRequest(badForm.errorsAsJson)
          }
        },
        (articleForm: ArticleForm) => {
          ArticleHandler.createArticle(articleForm)
            .map {
              article =>
                Created(Json.toJson(article))
            }
        }
      )
  }
  }

  def findById(id: Int): Action[AnyContent] = ArticleAction.async {
    implicit request =>
      printMessage
      ArticleHandler.selectById(id)
        .map {
          article =>
            Ok(Json.toJson(article))
        }
  }

  def printMessage(implicit request: ArticleRequest[AnyContent]): Unit = {
    println(s"message:${request.messagesApi.messages}")
    println(s"queryString:${request.queryString}")
    println(s"mediaType:${request.mediaType}")
    println(s"path:${request.path}")
  }

  def queryArticle(page: Int, size: Int): Action[AnyContent] = ArticleAction.async {
    implicit request =>
      request.body.asJson
      ArticleHandler.selectArticle(page, size)
        .map {
          result =>
            Ok(Json.toJson(result))
        }
  }

  def postArticle: Action[JsValue] = ArticlePostAction.async {
    implicit request =>
      Future {
        Ok("success")
      }
  }
}
