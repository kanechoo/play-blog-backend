package v1.api.router

import com.google.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._
import v1.api.controller.{ArchiveController, CategoryController}


class CategoryRouter @Inject()(ac: ArchiveController, cc: CategoryController) extends SimpleRouter {
  override def routes: Routes = {
    case GET(p"/$categoryName") =>
      ac.findByCategoryName(categoryName)
    case GET(p"/statistics/count") =>
      cc.findCategory
  }
}
