package v1.api.cont

import v1.api.entity.Article
import v1.api.json.{PageProductWrites, ProductWrites}

object Const {

  implicit val defaultJsonWrites: ProductWrites[Article] = new ProductWrites[Article]
  implicit val pageDefaultJsonWrites: PageProductWrites[Article] = new PageProductWrites[Article]
}

object Page {

  val page = "page"

  val size = "size"

  val total = "total"

  val data = "data"
}

object Entities {
  val categorySplitSymbol = "#"

  val tagSplitSymbol = "#"

  object CommonField {
    val id = "id"
  }


  object ArticleField {
    val table = "Article"

    val title = "title"

    val author = "author"

    val publishTime = "publishTime"

    val content = "content"

    val createTime = "createTime"
  }

  object CategoryField {
    val category = "category"
  }

  object TagField {
    val tag = "tag"
  }

}

object ArticleSql {
  val selectSql = "select ARTICLE.*,GROUP_CONCAT(DISTINCT CONCAT(C.ID,'#',C.CATEGORY)) as category ,GROUP_CONCAT(DISTINCT CONCAT(T.ID,'#',T.TAG)) as tag from ARTICLE left join ARTICLE_CATEGORY AC on ARTICLE.ID = AC.ARTICLE_ID left join ARTICLE_TAG AT on ARTICLE.ID = AT.ARTICLE_ID left join CATEGORY C on AC.CATEGORY_ID = C.ID left join TAG T on T.ID = AT.TAG_ID"
}

