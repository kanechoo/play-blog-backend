package v1.api.json

import play.api.libs.json._
import v1.api.cont.Page._
import v1.api.entity.SerialNumber
import v1.api.page.Page

import java.sql.Date

class ProductWrites[A <: Product] extends Writes[A] with DefaultWrites {
  override def writes(product: A): JsValue = {
    format(product)
  }

  def format[P <: Product](p: P): JsValue = {
    p.getClass.getDeclaredFields.foldRight(JsObject.empty) {
      (field, jsonObject) =>
        field.setAccessible(true)
        val jsonValue = any2JsonValue(field.get(p))
        jsonObject + (field.getName, jsonValue)
    }
  }

  def format[P <: Product](list: Seq[P]): JsValue = {
    list.foldRight(JsArray.empty) {
      (anyType, jsonArray) =>
        jsonArray.append(format(anyType))
    }
  }

  def any2JsonValue(any: Any): JsValue = any match {
    case _: Int | Long | Float | Double => JsNumber(any.asInstanceOf[Int])
    case _: String => JsString(any.asInstanceOf[String])
    case serialNumber: SerialNumber => JsNumber(serialNumber.id)
    case sqlDate: Date => JsNumber(sqlDate.asInstanceOf[Date].getTime)
    case javaDate: java.util.Date => JsNumber(javaDate.getTime)
    case p: Product => format(p)
    case anyList: Seq[_] => format(anyList.map(e => e.asInstanceOf[Product]))
    case _ => JsNull
  }
}

class PageProductWrites[A <: Product] extends ProductWrites[Page[A]] {
  override def writes(P: Page[A]): JsValue = {
    val result: JsObject = JsObject.empty
    val jsonArray = format(P.items)
    val totalP = Double.box(P.total.toDouble / maxItemSize).intValue() + 1
    result + (page, JsNumber(P.page)) + (size, JsNumber(P.size)) + (total, JsNumber(P.total)) + (totalPage, JsNumber(totalP)) + (data, jsonArray)
  }
}
