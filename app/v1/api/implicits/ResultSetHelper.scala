package v1.api.implicits

import v1.api.cont.Entities.ArticleField._
import v1.api.cont.Entities.CategoryField._
import v1.api.cont.Entities.CommonField._
import v1.api.cont.Entities.TagField._
import v1.api.entity.{Article, Category, SerialNumber, Tag}

import java.sql.{Date, PreparedStatement, ResultSet}
import scala.collection.immutable.Range

object ResultSetHelper {

  implicit class ResultSet2LazyList(resultSet: ResultSet) {
    def toLazyList: LazyList[ResultSet] = {
      new Iterator[ResultSet] {
        override def hasNext: Boolean = resultSet.next()

        override def next(): ResultSet = resultSet
      }.to(LazyList)
    }

    def getRowCount: Long = {
      new Iterator[Long] {
        override def hasNext: Boolean = resultSet.next()

        override def next(): Long = {
          resultSet.getInt(1).toLong
        }
      }.toList.head
    }

    def asArticle: Article = ArticleResultSet.map2Article(resultSet)

    def asCategory: Category = CategoryResultSet.map2Category(resultSet)

    def asTag: Tag = TagResultSet.map2Tag(resultSet)
  }

  implicit class PreparedStatementHelper(ps: PreparedStatement) {
    def getGeneratedIDs: List[Int] = {
      ps.getGeneratedKeys
        .toLazyList
        .map {
          rs =>
            rs.getInt(1)
        }
        .toList
    }
  }

}

object ArticleResultSet {
  def map2Article(implicit resultSet: ResultSet): Article = {
    val category = resultSet.getString("category")
      .split(",")
      .map {
        c =>
          val split = ArticleResultSet.split(c)
          Category(split._1, split._2)
      }
      .toSeq
    val tag = resultSet.getString("tag")
      .split(",")
      .map {
        t =>
          val split = ArticleResultSet.split(t)
          Tag(split._1, split._2)
      }
      .toSeq
    Article(
      SerialNumber(resultSet.getInt(id)),
      resultSet.getString(title),
      resultSet.getString(author),
      resultSet.getDate(publishTime),
      resultSet.getString(content),
      resultSet.getDate(createTime),
      category,
      tag
    )
  }

  def split(s: String): (SerialNumber, String) = {
    (SerialNumber(Integer.parseInt(s.split("#")(0))), s.split("#")(1))
  }
}

object CategoryResultSet {
  def map2Category(implicit resultSet: ResultSet): Category = {
    Category(SerialNumber(resultSet.getInt(id)), resultSet.getString(category))
  }

}

object TagResultSet {
  def map2Tag(implicit resultSet: ResultSet): Tag = {
    Tag(SerialNumber(resultSet.getInt(id)), resultSet.getString(tag))
  }
}

object ConnectionUtil {

  implicit class Params(ps: PreparedStatement) {
    /**
      * Set values to placeholder statement for PreparedStatement
      *
      * @param params values
      * @return PreparedStatement
      */
    def setParams(params: Any*): PreparedStatement = {
      for (index <- params.indices) {
        val value = params(index)
        val columnIndex = index + 1
        value match {
          case _: Int => ps.setInt(columnIndex, value.asInstanceOf[Int])
          case _: Long => ps.setLong(columnIndex, value.asInstanceOf[Long])
          case _: String => ps.setString(columnIndex, value.asInstanceOf[String])
          case _: Date => ps.setDate(columnIndex, value.asInstanceOf[Date])
          case _: java.util.Date => ps.setDate(columnIndex, new Date(value.asInstanceOf[java.util.Date].getTime))
          case _ =>
        }
      }
      ps
    }

    /**
      * Set values to placeholder statement for PreparedStatement,
      * support int and string type in query
      * like "select * from user where id in(?,?,?)"
      *
      * @param params values
      * @return preparedStatement
      */
    def setInParams(params: Seq[Any]): PreparedStatement = {
      for (index <- Range(0, params.length)) {
        val param = params(index)
        param match {
          case _: Int => ps.setInt(index + 1, param.asInstanceOf[Int])
          case _: String => ps.setString(index + 1, param.asInstanceOf[String])
          case _ =>
        }
      }
      ps
    }
  }

}

object PreparedStatementPlaceHolder {

  implicit class PlaceHolder(private val times: Int) {
    def params: String = {
      val holder = new StringBuilder
      Range(0, times)
        .foreach {
          i =>
            holder.append("?").append(",")
        }
      holder.deleteCharAt(holder.length - 1)
      holder.toString
    }
  }

}




