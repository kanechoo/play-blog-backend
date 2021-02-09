package v1.api.router

import com.google.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._
import v1.api.controller.{ArchiveController, TagController}

class TagRouter @Inject()(ac: ArchiveController, tc: TagController) extends SimpleRouter {
  override def routes: Routes = {
    case GET(p"/$tagName") =>
      ac.findByTagName(tagName)
    case GET(p"/statistics/count") =>
      tc.findTag
  }
}
