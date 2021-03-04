package v1.api.action

import com.google.inject.Inject
import play.api.http.{FileMimeTypes, HttpVerbs}
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._
import v1.api.entity.{ArchiveQueryHandler, PostRequestParams}
import v1.api.handler.ArchiveHandler
import v1.api.log.RequestLogMarker

import scala.concurrent.{ExecutionContext, Future}

class ArchiveRequest[A] @Inject()(request: Request[A], val messagesApi: MessagesApi, val archiveQueryParams: PostRequestParams) extends WrappedRequest(request)
  with PreferredMessagesProvider with ArchiveQueryHandler

class ArchiveActionBuilder @Inject()(playBodyParsers: PlayBodyParsers, messagesApi: MessagesApi)(implicit val executionContext: ExecutionContext) extends
  ActionBuilder[ArchiveRequest, AnyContent]
  with RequestLogMarker
  with HttpVerbs {

  import v1.api.implicits.RequestHandler._

  override def parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  override def invokeBlock[A](request: Request[A], block: ArchiveRequest[A] => Future[Result]): Future[Result] = {
    block(new ArchiveRequest(request, messagesApi, request.bindRequestQueryString))
      .map(result => {
        request.method match {
          case GET | HEAD =>
            result
          case POST =>
            result
          case _ =>
            result
        }
      })
  }
}

class ArchiveBaseController @Inject()(acc: ArchiveControllerComponents) extends BaseController with RequestLogMarker {
  def ArchiveAction: ArchiveActionBuilder = acc.archiveActionBuilder

  def ArchiveHandler: ArchiveHandler = acc.archiveHandler

  override protected def controllerComponents: ControllerComponents = acc

}

case class ArchiveControllerComponents @Inject()(archiveActionBuilder: ArchiveActionBuilder,
                                                 archiveHandler: ArchiveHandler,
                                                 actionBuilder: DefaultActionBuilder,
                                                 parsers: PlayBodyParsers,
                                                 messagesApi: MessagesApi,
                                                 langs: Langs,
                                                 fileMimeTypes: FileMimeTypes,
                                                 executionContext: ExecutionContext) extends ControllerComponents
