package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import support.hexo.MdReaderImpl
import util.CatalogUtil

import java.io.File
import scala.io.Source

class ArchiveRouterSpec extends PlaySpec with GuiceOneAppPerTest {

}

object mdTest {
  def main(args: Array[String]): Unit = {
    val t = new MdReaderImpl(null)(null)
    val file = "C:\\Users\\konc\\posts\\Hadoop FS Commands.md"
    val file1 = "C:\\Users\\konc\\posts\\Hexo博客搭建.md"
    val file2 = "C:\\Users\\konc\\posts\\JVM重要参数及其设置.md"
    val file3 = "C:\\Users\\konc\\posts\\Redis-5种基本数据结构.md"
    val reader = Source.fromFile(new File(file2), "utf8")
    val str = reader.mkString
    //    val toc = AtxMarkdownToc.newInstance
    //      .write(false)
    //      .charset("utf-8")
    //      .genTocFile(file2)
    //      .getTocLines
    //      .stream()
    //      .filter(x => !x.contains("Table of Contents"))
    //      .reduce((s1, s2) => s1 + "\n" + s2)
    //      .orElse("")
    val content = t.parseMd2Html(str, "         * [**Heap**内存配置](#heap内存配置)\n           * [**MetaSpace**内存配置](#metaspace内存配置)\n           * [选择**JVM**垃圾收集器](#选择jvm垃圾收集器)\n            * [**GC**活动日志记录](#gc活动日志记录)\n")
    val c = content.getOrElse("content", "")
    println(c)
    val catalog = CatalogUtil.parseHtml2Catalog(c, 3)
    println(catalog)
    reader.close()
  }


}