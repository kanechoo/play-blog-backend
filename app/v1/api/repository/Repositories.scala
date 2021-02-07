package v1.api.repository

import com.google.inject.Inject

case class Repositories @Inject()(archiveRepository: ArchiveRepository,
                                  categoryRepository: CategoryRepository,
                                  tagRepository: TagRepository,
                                  archiveCategoryRepository: ArchiveCategoryRepository,
                                  archiveTagRepository: ArchiveTagRepository)
