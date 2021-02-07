package v1.api.action

import com.google.inject.Inject
import play.api.http.{FileMimeTypes, HttpVerbs}
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc._
import v1.api.handler.ArchiveHandler
import v1.api.log.RequestLogMarker

import scala.concurrent.{ExecutionContext, Future}

class ArchiveRequest[A](request: Request[A], val messagesApi: MessagesApi) extends WrappedRequest(request) with PreferredMessagesProvider

class ArchivePostRequest[A](request: Request[A], val messagesApi: MessagesApi) extends WrappedRequest(request) with PreferredMessagesProvider

class ArchivePostActionBuilder @Inject()(bodyParsers: PlayBodyParsers, messagesApi: MessagesApi)(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[ArchivePostRequest, JsValue] with RequestLogMarker with HttpVerbs {

  override def parser: BodyParser[JsValue] = bodyParsers.json

  override def invokeBlock[A](request: Request[A], block: ArchivePostRequest[A] => Future[Result]): Future[Result] = {
    block(new ArchivePostRequest[A](request, messagesApi))
      .map {
        result =>
          result
      }
  }

}

class ArchiveActionBuilder @Inject()(playBodyParsers: PlayBodyParsers, messagesApi: MessagesApi)(implicit val executionContext: ExecutionContext) extends
  ActionBuilder[ArchiveRequest, AnyContent]
  with RequestLogMarker
  with HttpVerbs {
  override def parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  override def invokeBlock[A](request: Request[A], block: ArchiveRequest[A] => Future[Result]): Future[Result] = {
    block(new ArchiveRequest(request, messagesApi))
      .map(result => {
        request.method match {
          case GET | HEAD =>
            result.withHeaders("Cache-Control" -> s"max-age: 100")
          case other =>
            result
        }
      })
  }
}

class ArchiveBaseController @Inject()(acc: ArchiveControllerComponents) extends BaseController with RequestLogMarker {
  def ArchiveAction: ArchiveActionBuilder = acc.archiveActionBuilder

  def ArchivePostAction: ArchivePostActionBuilder = acc.archivePostActionBuilder

  def ArchiveHandler: ArchiveHandler = acc.archiveHandler

  override protected def controllerComponents: ControllerComponents = acc

}

case class ArchiveControllerComponents @Inject()(archiveActionBuilder: ArchiveActionBuilder,
                                                 archivePostActionBuilder: ArchivePostActionBuilder,
                                                 archiveHandler: ArchiveHandler,
                                                 actionBuilder: DefaultActionBuilder,
                                                 parsers: PlayBodyParsers,
                                                 messagesApi: MessagesApi,
                                                 langs: Langs,
                                                 fileMimeTypes: FileMimeTypes,
                                                 executionContext: ExecutionContext) extends ControllerComponents
