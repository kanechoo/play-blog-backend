package v1.api.handler

import com.google.inject.Inject
import play.api.Logger
import v1.api.entity.{Article, ArticleCategoryRel, ArticleForm, ArticleTagRel}
import v1.api.page.Page
import v1.api.repository.Repositories

import scala.concurrent.{ExecutionContext, Future}

class ArticleHandler @Inject()(dao: Repositories)(implicit ec: ExecutionContext) {
  val log: Logger = Logger(getClass)

  def createArticle(form: ArticleForm): Future[Article] = {

    dao.articleRepository.insertOne(form.getArticle)
      .map(aId => {
        log.debug(s"inserted article id : $aId")
        dao.categoryRepository.batchInsert(form.category)
          .map {
            cIds =>
              log.debug(s"inserted category ids : $cIds")
              dao.articleCategoryRepository
                .batchInsert(cIds.map(ArticleCategoryRel(aId.get, _)))
          }
        dao.tagRepository.batchInsert(form.tag)
          .map {
            tIds =>
              log.debug(s"inserted tag ids : $tIds")
              dao.articleTagRepository
                .batchInsert(tIds.map(ArticleTagRel(aId.get, _)))
          }
        form.getArticle
      })
  }

  def selectById(id: Int): Future[Option[Article]] = {
    dao.articleRepository.selectById(id)
      .map { article =>
        article
      }
  }

  def selectArticle(page: Int, size: Int): Future[Page[Article]] = {
    dao.articleRepository.list(page, size).map {
      result =>
        result
    }
  }
}
