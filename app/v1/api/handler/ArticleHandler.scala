package v1.api.handler

import com.google.inject.Inject
import play.api.Logger
import v1.api.entity.{Article, ArticleForm, SerialNumber}
import v1.api.page.Page
import v1.api.repository.{ArticleRepository, CategoryRepository, TagRepository}

import java.sql.Date
import scala.concurrent.{ExecutionContext, Future}

class ArticleHandler @Inject()(articleRepository: ArticleRepository,
                               categoryRepository: CategoryRepository,
                               tagRepository: TagRepository)
                              (implicit ec: ExecutionContext) {
  val log: Logger = Logger(getClass)

  def createArticle(articleForm: ArticleForm): Future[Article] = {
    val article = Article(
      SerialNumber(0),
      articleForm.title,
      articleForm.author,
      new Date(articleForm.publishTime.getTime),
      articleForm.content,
      new Date(System.currentTimeMillis()))
    articleRepository.insert(article)
      .map(id => {
        log.trace(s"inserted article id:$id")

        article
      })
  }

  def selectById(id: Int): Future[Option[Article]] = {
    articleRepository.select(id)
      .map { article =>
        article
      }
  }

  def selectArticle(page: Int, size: Int): Future[Page[Article]] = articleRepository.list(page, size).map {
    result =>
      result
  }
}
