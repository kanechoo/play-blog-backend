package v1.api.router

import com.google.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._
import v1.api.controller.ArticleController


class ArticleRouter @Inject()(ac: ArticleController) extends SimpleRouter {
  val linkPrefix = "/v1/api/"

  def link(id: Option[Int]): String = {
    linkPrefix + id.getOrElse(None)
  }

  def link(title: String): String = {
    linkPrefix + title
  }

  override def routes: Routes = {
    case POST(p"/") =>
      ac.createNewArticle
    case GET(p"/") & (q"page=$page" & q"size=$size") =>
      ac.queryArticle(page.toInt, size.toInt)
    case GET(p"/$id") =>
      ac.findById(Integer.parseInt(id))
    case POST(p"/test") =>
      println("success")
      ac.postArticle
  }
}
