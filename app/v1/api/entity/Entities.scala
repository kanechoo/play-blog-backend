package v1.api.entity

import v1.api.cont.Entities._

import java.sql.Date

case class Article(serialNumber: SerialNumber, title: String, author: String, publishTime: Date, content: String, createTime: Date, category: Seq[Category], tag: Seq[Tag])


case class Category(serialNumber: SerialNumber, category: String)

case class Tag(serialNumber: SerialNumber, tag: String)

case class ArticleCategoryRel(articleId: Int, categoryId: Int)

case class ArticleTagRel(articleId: Int, tagId: Int)


case class ArticleForm(title: String, author: String, publishTime: java.util.Date, content: String, category: Seq[Category], tag: Seq[Tag]) {
  def getArticle: Article = {
    Article(
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

object ArticleForm {
  def customApply(title: String, author: String, publishTime: java.util.Date, content: String, category: String, tag: String): ArticleForm = apply(title, author, publishTime, content, fmtCategory(category), fmtTag(tag))

  def fmtCategory(categoryOrTag: String): Seq[Category] = {
    if ((null == categoryOrTag || categoryOrTag.isEmpty) || !categoryOrTag.contains(categorySplitSymbol))
      Seq.empty
    else {
      categoryOrTag.split("#")
        .map {
          s =>
            Category(SerialNumber(0), s)
        }.toSeq
    }
  }

  def fmtTag(s: String): Seq[Tag] = {
    if ((null == s || s.isEmpty) || !s.contains(tagSplitSymbol))
      Seq.empty
    else {
      s.split("#")
        .map {
          s =>
            Tag(SerialNumber(0), s)
        }.toSeq
    }
  }

  def customUnApply(articleForm: ArticleForm): Option[(String, String, java.util.Date, String, String, String)] = {
    if (null == articleForm)
      None
    else {
      Some(articleForm.title,
        articleForm.author,
        articleForm.publishTime,
        articleForm.content,
        categoryToString(articleForm.category),
        tagToString(articleForm.tag))
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

class SerialNumber private(val id: Int) extends AnyVal

object SerialNumber {
  def apply(serialNumber: Int): SerialNumber = new SerialNumber(serialNumber)
}

