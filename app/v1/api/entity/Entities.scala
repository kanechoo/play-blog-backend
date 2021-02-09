package v1.api.entity

import v1.api.cont.Entities._

import java.sql.Date

case class Archive(serialNumber: SerialNumber, title: String, author: String, publishTime: Date, content: String, createTime: Date, category: Seq[Category], tag: Seq[Tag])


case class Category(serialNumber: SerialNumber, category: String)

case class CategoryCount(category: String, count: Int)

case class Tag(serialNumber: SerialNumber, tag: String)

case class TagCount(tag: String, count: Int)

case class ArchiveCategoryRel(archiveId: Int, categoryId: Int)

case class ArchiveTagRel(archiveId: Int, tagId: Int)

trait ArchiveQueryHandler {
  def archiveQueryParams: ArchiveQueryParams
}

case class ArchiveQueryParams(offset: Int, limit: Int, order: Option[String])

case class ArchiveForm(title: String, author: String, publishTime: java.util.Date, content: String, category: Seq[Category], tag: Seq[Tag]) {
  def getArchiveFormData: Archive = {
    Archive(
      SerialNumber(0),
      title,
      author,
      new Date(publishTime.getTime),
      content,
      new Date(System.currentTimeMillis()),
      category,
      tag
    )
  }
}

object ArchiveForm {
  def customApply(title: String, author: String, publishTime: java.util.Date, content: String, category: String, tag: String): ArchiveForm = apply(title, author, publishTime, content, fmtCategory(category), fmtTag(tag))

  def fmtCategory(categoryOrTag: String): Seq[Category] = {
    if (null == categoryOrTag || categoryOrTag.isEmpty)
      Seq.empty
    else {
      categoryOrTag.split(categorySplitSymbol)
        .map {
          s =>
            Category(SerialNumber(0), s)
        }.toSeq
    }
  }

  def fmtTag(s: String): Seq[Tag] = {
    if (null == s || s.isEmpty)
      Seq.empty
    else {
      s.split(tagSplitSymbol)
        .map {
          s =>
            Tag(SerialNumber(0), s)
        }.toSeq
    }
  }

  def customUnApply(archiveForm: ArchiveForm): Option[(String, String, java.util.Date, String, String, String)] = {
    if (null == archiveForm)
      None
    else {
      Some(archiveForm.title,
        archiveForm.author,
        archiveForm.publishTime,
        archiveForm.content,
        categoryToString(archiveForm.category),
        tagToString(archiveForm.tag))
    }


  }

  def categoryToString(category: Seq[Category]): String = {
    if (category.isEmpty) ""
    else {
      category.map(c => c.category).mkString(categorySplitSymbol)
    }
  }

  def tagToString(tag: Seq[Tag]): String = {
    if (tag.isEmpty) ""
    else {
      tag.map(t => t.tag).mkString(tagSplitSymbol)
    }
  }
}

case class SerialNumber private(id: Int) extends AnyVal

object SerialNumber {
  def apply(serialNumber: Int): SerialNumber = new SerialNumber(serialNumber)
}

