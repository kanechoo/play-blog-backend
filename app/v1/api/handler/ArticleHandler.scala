package v1.api.handler

import com.google.inject.Inject
import play.api.Logger
import v1.api.entity.{Article, ArticleForm}
import v1.api.page.Page
import v1.api.repository.Repositories

import scala.concurrent.{ExecutionContext, Future}

class ArticleHandler @Inject()(dao: Repositories)(implicit ec: ExecutionContext) {
  val log: Logger = Logger(getClass)

  def createArticle(form: ArticleForm): Future[Article] = {

    dao.articleRepository.insertOne(form.getArticle)
      .map(id => {
        log.debug(s"inserted article id : $id")
        dao.categoryRepository.batchInsert(form.category)
          .map {
            categoryId =>
              log.debug(s"inserted category ids : $categoryId")
          }
        dao.tagRepository.batchInsert(form.tag)
          .map {
            tagId =>
              log.debug(s"inserted tag ids : $tagId")
          }
        form.getArticle
      })
  }

  def selectById(id: Int): Future[Option[Article]] = {
    dao.articleRepository.select(id)
      .map { article =>
        article
      }
  }

  def selectArticle(page: Int, size: Int): Future[Page[Article]] = dao.articleRepository.list(page, size).map {
    result =>
      result
  }
}
