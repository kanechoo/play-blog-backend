package v1.api.entity

import java.sql.{Date, ResultSet}

trait SqlEntity[T] {
  def mapping(resultSet: ResultSet): T

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

  def sql_insert(tType: T): String = {
    val insertSql = s"insert into $table "
    val names = new StringBuilder
    val values = new StringBuilder
    names.append("(")
    values.append("(")
    val map = tType.getClass.getDeclaredFields
      .filterNot(f => f.getName.toLowerCase.matches(".*serial.*"))
      .map {
        f =>
          f.setAccessible(true)
          Map(f.getName -> f.get(tType))
      }.reduce((a, b) => a ++ b)
    names.append(map.keys.mkString(",")).append(")")
    values.append("'").append(map.values.mkString("','")).append("')")
    insertSql + names + " values " + values
  }

  def table: String = "[A-Za-z]".r().findAllIn(getClass.getSimpleName).mkString("")

}

case class Article(serialNumber: SerialNumber, title: String, author: String, publishTime: Date, content: String, createTime: Date)

object Article extends SqlEntity[Article] {
  override def mapping(resultSet: ResultSet): Article = {
    Article(
      SerialNumber(resultSet.getInt(id)),
      resultSet.getString(title),
      resultSet.getString(author),
      resultSet.getDate(publishTime),
      resultSet.getString(content),
      resultSet.getDate(createTime)
    )
  }

  def id = "id"

  def title = "title"

  def author = "author"

  def publishTime = "publishTime"

  def content = "content"

  def createTime = "createTime"

}

class SerialNumber private(val id: Int) extends AnyVal

object SerialNumber {
  def apply(serialNumber: Int): SerialNumber = new SerialNumber(serialNumber)
}

