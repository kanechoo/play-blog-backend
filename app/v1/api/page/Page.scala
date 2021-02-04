package v1.api.page

case class Page[+A](items: List[A], page: Int, size: Int, total: Long)
