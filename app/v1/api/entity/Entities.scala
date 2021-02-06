package v1.api.entity

import play.api.Logger
import v1.api.cont.Entities._

import java.sql.Date

trait SqlStatement[T] {
  val log: Logger = Logger(getClass)

  def sql_select_by_id(id: Int): String = {
    sql_select_all + s" where id=$id "
  }

  def sql_count: String = {
    s"select count(*) from $table"
  }

  def sql_select_limit(offset: Int, size: Int): String = {
    val safeOffset = Math.max(1, offset)
    val safeSize = Math.min(10, size)
    sql_select_all + s" limit ${safeOffset - 1},$safeSize"
  }

  def sql_select_all: String = {
    s"select * from $table"
  }

  def sql_insert(tType: T): String = {
    val insertSql = s"insert into $table "
    val names = new StringBuilder
    val values = new StringBuilder
    names.append("(")
    values.append("(")
    val map = tType.getClass.getDeclaredFields
      .filterNot(f => f.getName.toLowerCase.matches(".*serial.*"))
      .map { f =>
        f.setAccessible(true)
        Map(f.getName -> f.get(tType))
      }
      .reduce((a, b) => a ++ b)
    names.append(map.keys.mkString(",")).append(")")
    values.append("'").append(map.values.mkString("','")).append("')")
    val statement = insertSql + names + " values " + values
    log.debug(s"insert statement ===> $statement")
    statement
  }

  def table: String = "[A-Za-z]".r().findAllIn(getClass.getSimpleName).mkString("")

}


case class Article(serialNumber: SerialNumber, title: String, author: String, publishTime: Date, content: String, createTime: Date)

object Article extends SqlStatement[Article] {}

case class Category(serialNumber: SerialNumber, category: String)

object Category extends SqlStatement[Category] {}

case class Tag(serialNumber: SerialNumber, tag: String)

object Tag extends SqlStatement[Tag] {}

case class ArticleForm(title: String, author: String, publishTime: java.util.Date, content: String, category: Seq[Category], tag: Seq[Tag]) {
  def getArticle: Article = {
    Article(
      SerialNumber(0),
      title,
      author,
      new Date(publishTime.getTime),
      content,
      new Date(System.currentTimeMillis()))
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

