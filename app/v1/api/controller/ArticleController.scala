package v1.api.controller

import com.google.inject.Inject
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import v1.api.action.{ArticleBaseController, ArticleControllerComponents}
import v1.api.cont.ConstVal._

import java.util.Date
import scala.concurrent.{ExecutionContext, Future}

case class ArticleForm(title: String, author: String, publishTime: Date, content: String)

class ArticleController @Inject()(acc: ArticleControllerComponents)(implicit ec: ExecutionContext)
  extends ArticleBaseController(acc) {
  private val form: Form[ArticleForm] = {
    import play.api.data.Forms._
    Form(
      mapping(
        "title" -> nonEmptyText,
        "author" -> nonEmptyText,
        "publishTime" -> date,
        "content" -> nonEmptyText
      )(ArticleForm.apply)(ArticleForm.unapply)
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
      ArticleHandler.selectById(id)
        .map {
          article =>
            Ok(Json.toJson(article))
        }
  }

  def queryArticle(page: Int, size: Int): Action[AnyContent] = ArticleAction.async {
    implicit request =>
      ArticleHandler.selectArticle(page, size)
        .map {
          result =>
            Ok(Json.toJson(result))
        }
  }
}
