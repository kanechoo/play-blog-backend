package v1.api.data

import java.sql.{Date, PreparedStatement}

object SqlUtil {

  implicit class PreparedStatementValue[A](preparedStatement: PreparedStatement) {
    def setValues(A: A): PreparedStatement = {
      val fields = A.getClass.getDeclaredFields
      for (index <- Range(1, fields.length)) {
        val field = fields(index)
        field.setAccessible(true)
        field.get(A) match {
          case _: Integer => preparedStatement.setInt(index, field.getInt(A))
          case _: String =>
            preparedStatement.setString(index, field.get(A).toString)
          case _: Date =>
            preparedStatement.setDate(index, new Date(field.get(A).asInstanceOf[java.util.Date].getTime))
          case _ =>
        }
      }
      preparedStatement
    }
  }

}

