package v1.api.cont

import v1.api.entity.Archive
import v1.api.json.{PageProductWrites, ProductWrites}

object Const {

  implicit val defaultJsonWrites: ProductWrites[Archive] = new ProductWrites[Archive]
  implicit val pageDefaultJsonWrites: PageProductWrites[Archive] = new PageProductWrites[Archive]
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


  object ArchiveField {
    val table = "Archive"

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

object ArchiveSql {
  val selectSql = "select ARCHIVE.*,GROUP_CONCAT(DISTINCT CONCAT(C.ID,'#',C.CATEGORY)) as category ,GROUP_CONCAT(DISTINCT CONCAT(T.ID,'#',T.TAG)) as tag from ARCHIVE left join ARCHIVE_CATEGORY AC on ARCHIVE.ID = AC.ARCHIVE_ID left join ARCHIVE_TAG AT on ARCHIVE.ID = AT.ARCHIVE_ID left join CATEGORY C on AC.CATEGORY_ID = C.ID left join TAG T on T.ID = AT.TAG_ID group by AC.ARCHIVE_ID limit ? offset ?"
  val selectByIdSql: String =
    """
      |select ARCHIVE.*,
      |       GROUP_CONCAT(DISTINCT CONCAT(C.ID, '#', C.CATEGORY)) as category,
      |       GROUP_CONCAT(DISTINCT CONCAT(T.ID, '#', T.TAG))      as tag
      |from ARCHIVE
      |         left join ARCHIVE_CATEGORY AC on ARCHIVE.ID = AC.ARCHIVE_ID
      |         left join ARCHIVE_TAG AT on ARCHIVE.ID = AT.ARCHIVE_ID
      |         left join CATEGORY C on AC.CATEGORY_ID = C.ID
      |         left join TAG T on T.ID = AT.TAG_ID
      |where ARCHIVE.ID=?
      |group by ARCHIVE.ID
      |""".stripMargin
  val selectByCategoryNameSql: String =
    """
      |select ARCHIVE.*,
      |       GROUP_CONCAT(DISTINCT CONCAT(C.ID, '#', C.CATEGORY)) as category,
      |       GROUP_CONCAT(DISTINCT CONCAT(T.ID, '#', T.TAG))      as tag
      |from ARCHIVE
      |         left join ARCHIVE_CATEGORY AC on ARCHIVE.ID = AC.ARCHIVE_ID
      |         left join ARCHIVE_TAG AT on ARCHIVE.ID = AT.ARCHIVE_ID
      |         left join CATEGORY C on AC.CATEGORY_ID = C.ID
      |         left join TAG T on T.ID = AT.TAG_ID
      |where C.CATEGORY=?
      |group by ARCHIVE.ID
      |limit ? offset ?
      |""".stripMargin
  val selectCountByCategoryNameSql: String =
    """
      |select count(*)
      |from ARCHIVE
      |         left join ARCHIVE_CATEGORY AC on ARCHIVE.ID = AC.ARCHIVE_ID
      |         left join CATEGORY C on AC.CATEGORY_ID = C.ID
      |where C.CATEGORY=?
      |group by C.ID
      |""".stripMargin
  val selectByTagNameSql: String =
    """
      |select ARCHIVE.*,
      |       GROUP_CONCAT(DISTINCT CONCAT(C.ID, '#', C.CATEGORY)) as category,
      |       GROUP_CONCAT(DISTINCT CONCAT(T.ID, '#', T.TAG))      as tag
      |from ARCHIVE
      |         left join ARCHIVE_CATEGORY AC on ARCHIVE.ID = AC.ARCHIVE_ID
      |         left join ARCHIVE_TAG AT on ARCHIVE.ID = AT.ARCHIVE_ID
      |         left join CATEGORY C on AC.CATEGORY_ID = C.ID
      |         left join TAG T on T.ID = AT.TAG_ID
      |where T.TAG=?
      |group by ARCHIVE.ID
      |limit ? offset ?
      |""".stripMargin
  val selectCountByTagNameSql: String =
    """
      |select count(*)
      |from ARCHIVE
      |         left join ARCHIVE_TAG AT on ARCHIVE.ID = AT.ARCHIVE_ID
      |         left join TAG T on T.ID = AT.TAG_ID
      |where T.TAG=?
      |group by T.ID
      |""".stripMargin
}

