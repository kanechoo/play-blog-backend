package v1.api.page


case class Page[+A](items: Seq[A], page: Int, size: Int, total: Long, totalPage: Int*)
