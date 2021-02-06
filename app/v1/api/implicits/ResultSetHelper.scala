package v1.api.implicits

import v1.api.cont.Entities.ArticleField._
import v1.api.cont.Entities.CategoryField._
import v1.api.cont.Entities.CommonField._
import v1.api.cont.Entities.TagField._
import v1.api.entity.{Article, Category, SerialNumber, Tag}

import java.sql.{Connection, Date, PreparedStatement, ResultSet}

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
    Article(
      SerialNumber(resultSet.getInt(id)),
      resultSet.getString(title),
      resultSet.getString(author),
      resultSet.getDate(publishTime),
      resultSet.getString(content),
      resultSet.getDate(createTime),
      null,
      null
    )
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

  implicit class PreparedSql(conn: Connection) {
    def preparedSql(sql: String, statementOption: Int*): PreparedStatement = {
      statementOption.map {
        id =>
          conn.prepareStatement(sql, id)
      }
      conn.prepareStatement(sql)
    }
  }

  implicit class Params(ps: PreparedStatement) {
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
  }

}




