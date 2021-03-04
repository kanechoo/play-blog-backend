package v1.api.cont

import play.api.libs.json.{Format, Json, OFormat}
import v1.api.entity._
import v1.api.json.{PageProductWrites, ProductWrites}

trait JsonWrites {
  implicit val archiveJsonWrites: ProductWrites[Archive] = new ProductWrites[Archive]
  implicit val pageArchiveWrites: PageProductWrites[Archive] = new PageProductWrites[Archive]
  implicit val focusArchiveWrites: ProductWrites[FocusArchive] = new ProductWrites[FocusArchive]
  implicit val tagCountJsonWrites: OFormat[TagCount] = Json.format[TagCount]
  implicit val categoryCountJsonWrites: OFormat[CategoryCount] = Json.format[CategoryCount]
  implicit val statusWrites: Format[ResponseMessage] = Json.format[ResponseMessage]
  implicit val timelineWrites: PageProductWrites[Timeline] = new PageProductWrites[Timeline]
}

object DefaultValues {
  val defaultCategory = "未分类"
  val defaultTag = "未分类"
  val defaultSerialNumber: SerialNumber = SerialNumber(0)
}

object Page {

  val page = "page"

  val size = "size"

  val total = "total"

  val data = "list"

  val totalPage = "totalPage"
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
    val catalog = "catalog"
  }

  object CategoryField {
    val category = "category"
  }

  object TagField {
    val tag = "tag"
  }

}


object ArchiveSql {
  val selectSql = "select ARCHIVE.*,GROUP_CONCAT(DISTINCT CONCAT(C.ID,'#',C.CATEGORY)) as category ,GROUP_CONCAT(DISTINCT CONCAT(T.ID,'#',T.TAG)) as tag from ARCHIVE left join ARCHIVE_CATEGORY AC on ARCHIVE.ID = AC.ARCHIVE_ID left join ARCHIVE_TAG AT on ARCHIVE.ID = AT.ARCHIVE_ID left join CATEGORY C on AC.CATEGORY_ID = C.ID left join TAG T on T.ID = AT.TAG_ID group by ARCHIVE.ID  order by ARCHIVE.PUBLISHTIME DESC  limit ? offset ?"
  val selectByIdSql =
    """
      select ARCHIVE.*,
             GROUP_CONCAT(DISTINCT CONCAT(C.ID, '#', C.CATEGORY)) as category,
             GROUP_CONCAT(DISTINCT CONCAT(T.ID, '#', T.TAG))      as tag
      from ARCHIVE
               left join ARCHIVE_CATEGORY AC on ARCHIVE.ID = AC.ARCHIVE_ID
                 left join CATEGORY C on AC.CATEGORY_ID = C.ID
               left join ARCHIVE_TAG AT on ARCHIVE.ID = AT.ARCHIVE_ID
               left join TAG T on T.ID = AT.TAG_ID
      where ARCHIVE.ID=?
      group by ARCHIVE.ID
      order by ARCHIVE.publishTime DESC
      """
  val selectByCategoryNameSql =
    """
      select ARCHIVE.*,
             GROUP_CONCAT(DISTINCT CONCAT(C.ID, '#', C.CATEGORY)) as category,
             GROUP_CONCAT(DISTINCT CONCAT(T.ID, '#', T.TAG))      as tag
      from ARCHIVE
               left join ARCHIVE_CATEGORY AC on ARCHIVE.ID = AC.ARCHIVE_ID
                left join CATEGORY C on AC.CATEGORY_ID = C.ID
               left join ARCHIVE_TAG AT on ARCHIVE.ID = AT.ARCHIVE_ID
               left join TAG T on T.ID = AT.TAG_ID
      where ARCHIVE.ID in (select ARCHIVE.ID
                           from ARCHIVE
                                    left join ARCHIVE_CATEGORY AC on AC.ARCHIVE_ID =ARCHIVE.ID
                                    left join CATEGORY C on C.ID= AC.CATEGORY_ID
                           where C.CATEGORY = ?)
      group by ARCHIVE.ID
       order by ARCHIVE.publishTime DESC
      limit ? offset ?
    """
  val selectCountByCategoryNameSql =
    """
      select count(*)
      from ARCHIVE
               left join ARCHIVE_CATEGORY AC on ARCHIVE.ID = AC.ARCHIVE_ID
               left join CATEGORY C on AC.CATEGORY_ID = C.ID
      where C.CATEGORY=?
      group by C.ID
      """
  val selectByTagNameSql =
    """
      select ARCHIVE.*,
             GROUP_CONCAT(DISTINCT CONCAT(C.ID, '#', C.CATEGORY)) as category,
             GROUP_CONCAT(DISTINCT CONCAT(T.ID, '#', T.TAG))      as tag
      from ARCHIVE
               left join ARCHIVE_CATEGORY AC on ARCHIVE.ID = AC.ARCHIVE_ID
               left join CATEGORY C on AC.CATEGORY_ID = C.ID
               left join ARCHIVE_TAG AT on ARCHIVE.ID = AT.ARCHIVE_ID
               left join TAG T on T.ID = AT.TAG_ID
      where ARCHIVE.ID in (select ARCHIVE.ID
                           from ARCHIVE
                                    left join ARCHIVE_TAG AT on ARCHIVE.ID = AT.ARCHIVE_ID
                                    left join TAG T on T.ID = AT.TAG_ID
                           where T.TAG = ?)
      group by ARCHIVE.ID
      order by ARCHIVE.publishTime DESC
      limit ? offset ?
    """
  val selectCountByTagNameSql =
    """
     select count(*)
     from ARCHIVE
              left join ARCHIVE_TAG AT on ARCHIVE.ID = AT.ARCHIVE_ID
              left join TAG T on T.ID = AT.TAG_ID
     where T.TAG=?
     group by T.ID
     """
  val searchSql =
    """
         select ARCHIVE.*,
               GROUP_CONCAT(DISTINCT CONCAT(C.ID, '#', C.CATEGORY)) as category,
                GROUP_CONCAT(DISTINCT CONCAT(T.ID, '#', T.TAG))      as tag
         from ARCHIVE
                  left join ARCHIVE_CATEGORY AC on ARCHIVE.ID = AC.ARCHIVE_ID
                  left join ARCHIVE_TAG AT on ARCHIVE.ID = AT.ARCHIVE_ID
                  left join CATEGORY C on AC.CATEGORY_ID = C.ID
                  left join TAG T on T.ID = AT.TAG_ID
         where LOWER(ARCHIVE.TITLE ) like  CONCAT( '%',?,'%')
            OR LOWER(C.CATEGORY) = ?
            OR LOWER( T.TAG)  = ?
         group by ARCHIVE.ID
         order by ARCHIVE.publishTime DESC
         """
}

