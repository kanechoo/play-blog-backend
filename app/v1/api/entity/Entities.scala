package v1.api.entity

import v1.api.cont.Entities.ArticleField._
import v1.api.cont.Entities.CategoryField._
import v1.api.cont.Entities.CommonField._
import v1.api.cont.Entities.TagField._
import v1.api.cont.Entities._

import java.sql.{Date, ResultSet}

trait EntityTable[T] {
  def map2Entity(resultSet: ResultSet): T

  def sql_select_by_id(id: Int): String = {
    sql_select_all + s" where id=$id "
  }

  def sql_count: String = {
    s"select count(*) from $table"
  }

  def sql_select_limit(offset: Int, size: Int): String = {
    sql_select_all + s" limit ${offset - 1},$size"
  }

  def sql_select_all: String = {
    s"select * from $table"
  }

  def table: String = "[A-Za-z]".r().findAllIn(getClass.getSimpleName).mkString("")

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
    insertSql + names + " values " + values
  }

}


case class Article(serialNumber: SerialNumber, title: String, author: String, publishTime: Date, content: String, createTime: Date)

case class Category(serialNumber: SerialNumber, category: String)

case class Tag(serialNumber: SerialNumber, tag: String)

case class ArticleForm(title: String, author: String, publishTime: java.util.Date, content: String, category: Seq[Category], tag: Seq[Tag])

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


object Article extends EntityTable[Article] {
  override def map2Entity(resultSet: ResultSet): Article = {
    Article(
      SerialNumber(resultSet.getInt(id)),
      resultSet.getString(title),
      resultSet.getString(author),
      resultSet.getDate(publishTime),
      resultSet.getString(content),
      resultSet.getDate(createTime)
    )
  }

}

object Category extends EntityTable[Category] {
  override def map2Entity(resultSet: ResultSet): Category = {
    Category(SerialNumber(resultSet.getInt(id)), resultSet.getString(category))
  }

}

object Tag extends EntityTable[Tag] {
  override def map2Entity(resultSet: ResultSet): Tag = {
    Tag(SerialNumber(resultSet.getInt(id)), resultSet.getString(tag))
  }
}

class SerialNumber private(val id: Int) extends AnyVal

object SerialNumber {
  def apply(serialNumber: Int): SerialNumber = new SerialNumber(serialNumber)
}

