package v1.api.cont

import v1.api.entity.Article
import v1.api.json.{PageProductWrites, ProductWrites}

object ConstVal {

  implicit val defaultJsonWrites: ProductWrites[Article] = new ProductWrites[Article]
  implicit val pageDefaultJsonWrites: PageProductWrites[Article] = new PageProductWrites[Article]

  def page = "page"

  def size = "size"

  def total = "total"

  def data = "data"
}

object Entities {
  val categorySplitSymbol = "#"

  val tagSplitSymbol = "#"

  object CommonField {
    val id = "id"
  }

  object ArticleField {

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
