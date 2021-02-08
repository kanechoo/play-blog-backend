package v1.api.router

import com.google.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._
import v1.api.controller.ArchiveController


class CategoryRouter @Inject()(ac: ArchiveController) extends SimpleRouter {
  override def routes: Routes = {
    case GET(p"/$categoryName") =>
      ac.findByCategoryName(categoryName)
  }
}
