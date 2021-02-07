package v1.api.handler

import com.google.inject.Inject
import play.api.Logger
import v1.api.entity.{Archive, ArchiveCategoryRel, ArchiveForm, ArchiveTagRel}
import v1.api.page.Page
import v1.api.repository.Repositories

import scala.concurrent.{ExecutionContext, Future}

class ArchiveHandler @Inject()(dao: Repositories)(implicit ec: ExecutionContext) {
  val log: Logger = Logger(getClass)

  def createArchive(form: ArchiveForm): Future[Archive] = {

    dao.archiveRepository.insertOne(form.getArchive)
      .map(aId => {
        log.debug(s"inserted archive id : $aId")
        dao.categoryRepository.batchInsert(form.category)
          .map {
            cIds =>
              log.debug(s"inserted category ids : $cIds")
              dao.archiveCategoryRepository
                .batchInsert(cIds.map(ArchiveCategoryRel(aId.get, _)))
          }
        dao.tagRepository.batchInsert(form.tag)
          .map {
            tIds =>
              log.debug(s"inserted tag ids : $tIds")
              dao.archiveTagRepository
                .batchInsert(tIds.map(ArchiveTagRel(aId.get, _)))
          }
        form.getArchive
      })
  }

  def selectById(id: Int): Future[Option[Archive]] = {
    dao.archiveRepository.selectById(id)
      .map { archive =>
        archive
      }
  }

  def selectArchive(page: Int, size: Int): Future[Page[Archive]] = {
    dao.archiveRepository.list(page, size).map {
      result =>
        result
    }
  }
}
