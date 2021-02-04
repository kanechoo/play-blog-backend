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
