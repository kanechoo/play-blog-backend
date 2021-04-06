package util

import v1.api.entity.CatalogMeta

import java.util.UUID
import scala.util.matching.Regex

object CatalogUtil {
  def parseHtml2Catalog(s: String, depth: Int): String = {
    val regex = getMatchRegex
    var pre: Option[Int] = None
    var data: List[List[CatalogMeta]] = List()
    var break: List[CatalogMeta] = List()
    val minDepth = regex.findAllIn(s)
      .map(s => judgeDepth(s))
      .minOption
      .getOrElse(7)
    val adaptorDepth = Math.min(minDepth + depth, 7)
    regex.findAllIn(s)
      .filter(judgeDepth(_) < adaptorDepth)
      .foreach {
        h =>
          val depth = judgeDepth(h)
          val aTag = h.replaceAll(s"(<h$depth.*?>)|(</h$depth>)", "")
          val a = aTag.replaceAll("id\\s*=\\s*\".*?\"", "")
          val c = CatalogMeta(depth, a)
          if (pre.isDefined && depth < pre.get) {
            data = data.appended(break)
            break = List()
            break = break.appended(c)
            pre = Some(depth)
          }
          else {
            break = break.appended(c)
            pre = Some(depth)
          }
      }

    if (break.nonEmpty) data = data.appended(break)
    val result = data.map {
      d =>
        val group = d.groupBy(x => x.depth)
        val list = group.tail.map {
          l =>
            "<ul>" + l._2.map(ll => s"<li>${ll.aTag}</li>").mkString + "</ul>"
        }.toList
        val combine1 = combine(list)
        group.head._2
          .zipWithIndex
          .map(y => {
            s"<li>${y._1.aTag}$combine1</li>"
          }).mkString
    }.mkString
    s"<ul>$result</ul>"
  }

  private def combine(list: List[String]): String = {
    var result = ""
    for (index <- list.indices) {
      val cur = list(index)
      if (index == 0) {
        result = cur
      }
      else result = result.dropRight(index * "</ul>".length) + cur + List.fill(index)("</ul>").mkString
    }
    result
  }

  def renderHeaderId2RandomUUID(html: String): String = {
    val regex = getMatchRegex
    var result = html
    regex.findAllIn(html)
      .foreach {
        h =>
          val uid = UUID.randomUUID().toString
          val depth = judgeDepth(h)
          val innerTagText = getInnerTagText(h)
          val newHeader = s"<h$depth id='${uid}'><a href='#${uid}'>${innerTagText}</a></h$depth>"
          result = result.replace(h, newHeader)
      }
    result
  }

  private def getMatchRegex: Regex = {
    s"<h[1-6].*?>.+?</h[1-6]>".r
  }

  private def judgeDepth(header: String): Int = {
    "</h[1-6]>".r.findAllIn(header)
      .map {
        s =>
          s.replaceAll("</h|>", "")
      }
      .toList
      .headOption
      .getOrElse("1")
      .toInt
  }

  def getInnerTagText(header: String): String = {
    "<a.+?>.+?</a>".r
      .findAllIn(header)
      .nextOption()
      .getOrElse("")
      .replaceAll("<a.+?>|</a>", "")
  }
}
