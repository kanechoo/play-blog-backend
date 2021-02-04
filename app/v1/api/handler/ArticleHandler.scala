package v1.api.handler

import com.google.inject.Inject
import v1.api.controller.ArticleForm
import v1.api.entity.{Article, SerialNumber}
import v1.api.page.Page
import v1.api.repository.ArticleRepository

import java.sql.Date
import scala.concurrent.{ExecutionContext, Future}

class ArticleHandler @Inject()(repository: ArticleRepository)(implicit ec: ExecutionContext) {
  def createArticle(articleForm: ArticleForm): Future[Article] = {
    val article = Article(
      SerialNumber(0),
      articleForm.title,
      articleForm.author,
      new Date(articleForm.publishTime.getTime),
      articleForm.content,
      new Date(System.currentTimeMillis()))
    repository.insert(article)
      .map(id => {
        article
      })
  }

  def selectById(id: Int): Future[Option[Article]] = {
    repository.select(id)
      .map { article =>
        article
      }
  }

  def selectArticle(page: Int, size: Int): Future[Page[Article]] = repository.list(page, size).map {
    result =>
      result
  }
}
