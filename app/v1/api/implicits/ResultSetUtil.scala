package v1.api.implicits

import v1.api.cont.Entities.CategoryField._
import v1.api.cont.Entities.CommonField._
import v1.api.cont.Entities.TagField._
import v1.api.entity.{Category, SerialNumber, Tag}

import java.sql.{PreparedStatement, ResultSet}

object ResultSetUtil {

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
    def getGeneratedID: Int = {
      val rs = ps.getGeneratedKeys
      var id: Int = 0
      while (rs.next()) {
        id = rs.getInt(1)
      }
      id
    }
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




