package v1.api.repository

import com.google.inject.Inject

case class Repositories @Inject()(articleRepository: ArticleRepository,
                                  categoryRepository: CategoryRepository,
                                  tagRepository: TagRepository,
                                  articleCategoryRepository: ArticleCategoryRepository,
                                  articleTagRepository: ArticleTagRepository)
