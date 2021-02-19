package v1.api.handler

import com.google.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc.AnyContent
import v1.api.action.ArchiveRequest
import v1.api.entity._
import v1.api.page.Page
import v1.api.repository.Repositories

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ArchiveHandler @Inject()(dao: Repositories)(implicit ec: ExecutionContext) {
  val log: Logger = Logger(getClass)

  def createArchive(archive: Archive): Future[Archive] = {

    dao.archiveRepository.insertOne(archive)
      .map(aId => {
        log.debug(s"inserted archive id : $aId")
        dao.categoryRepository.batchInsert(archive.category)
          .map {
            cIds =>
              log.debug(s"inserted category ids : $cIds")
              dao.archiveCategoryRepository
                .batchInsert(cIds.map(ArchiveCategoryRel(aId.get, _)))
          }
        dao.tagRepository.batchInsert(archive.tag)
          .map {
            tIds =>
              log.debug(s"inserted tag ids : $tIds")
              dao.archiveTagRepository
                .batchInsert(tIds.map(ArchiveTagRel(aId.get, _)))
          }
        archive
      })
  }

  def selectById(id: Int): Future[Option[FocusArchive]] = {
    dao.archiveRepository.selectById(id)
      .map { archive =>
        archive
      }
  }

  def selectArchive(params: ArchiveQueryParams): Future[Page[Archive]] = {
    dao.archiveRepository.list(params).map {
      result =>
        result
    }
  }

  def selectByCategoryName(categoryName: String)(implicit request: ArchiveRequest[AnyContent]): Future[Page[Archive]] = {
    dao.archiveRepository
      .selectByCategory(categoryName)
  }

  def selectByTagName(tagName: String)(implicit request: ArchiveRequest[AnyContent]): Future[Page[Archive]] = {
    dao.archiveRepository
      .selectByTag(tagName)
  }

  def deletePostById(postId: Int): Future[ResponseMessage] = {
    dao.archiveRepository
      .deleteById(postId)
  }

  def timeline(params: ArchiveQueryParams): Future[Page[Timeline]] = {
    dao.archiveRepository
      .timeline(params)

  }
}
