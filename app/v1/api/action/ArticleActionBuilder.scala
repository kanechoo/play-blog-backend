package v1.api.action

import com.google.inject.Inject
import play.api.http.{FileMimeTypes, HttpVerbs}
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._
import v1.api.handler.ArticleHandler
import v1.api.log.RequestLogMarker

import scala.concurrent.{ExecutionContext, Future}

class ArticleRequest[A](request: Request[A], val messagesApi: MessagesApi) extends WrappedRequest(request) with PreferredMessagesProvider

class ArticleActionBuilder @Inject()(playBodyParsers: PlayBodyParsers, messagesApi: MessagesApi)(implicit val executionContext: ExecutionContext) extends
  ActionBuilder[ArticleRequest, AnyContent]
  with RequestLogMarker
  with HttpVerbs {
  override def parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  override def invokeBlock[A](request: Request[A], block: ArticleRequest[A] => Future[Result]): Future[Result] = {
    recodeRequestContextLog(request)
    block(new ArticleRequest(request, messagesApi))
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

class ArticleBaseController @Inject()(acc: ArticleControllerComponents) extends BaseController with RequestLogMarker {
  def ArticleAction: ArticleActionBuilder = acc.articleActionBuilder

  def ArticleHandler: ArticleHandler = acc.articleHandler

  override protected def controllerComponents: ControllerComponents = acc
}

case class ArticleControllerComponents @Inject()(articleActionBuilder: ArticleActionBuilder,
                                                 articleHandler: ArticleHandler,
                                                 actionBuilder: DefaultActionBuilder,
                                                 parsers: PlayBodyParsers,
                                                 messagesApi: MessagesApi,
                                                 langs: Langs,
                                                 fileMimeTypes: FileMimeTypes,
                                                 executionContext: ExecutionContext) extends ControllerComponents
