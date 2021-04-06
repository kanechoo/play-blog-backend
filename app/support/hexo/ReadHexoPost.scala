package support.hexo

import akka.actor.ActorSystem
import com.google.inject.{Inject, Singleton}
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.core.IndentedCodeBlockParser
import com.vladsch.flexmark.parser.{Parser, PegdownExtensions}
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter
import com.vladsch.flexmark.util.data.{DataHolder, DataKey, MutableDataSet}
import play.api.Logger
import util.CatalogUtil
import v1.api.cont.DefaultValues._
import v1.api.entity.{Archive, Category, SerialNumber, Tag}
import v1.api.execute.DataBaseExecuteContext
import v1.api.handler.ArchiveHandler

import java.io.File
import java.sql.Date
import java.text.SimpleDateFormat
import java.util
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.io.Source

@Singleton
class MdReaderImpl @Inject()(archiveHandler: ArchiveHandler)(implicit executionContext: ExecutionContext) extends MdReader with Runnable {
  val log: Logger = Logger(this.getClass)

  override def run(): Unit = {
    readMd()
  }

  override def readMd(): Unit = {
    val mdFileRootPath = System.getProperty("user.home") + "/posts"
    val mdFileRoot = new File(mdFileRootPath)
    val archives = mdFileRoot.listFiles()
      .filter(_.getAbsolutePath.endsWith(".md"))
      .map {
        file =>
          val buffer = Source.fromFile(file, "utf-8")
          val content = buffer.mkString
          buffer.close()
          (file.getName.replaceAll("\\.(.*)", ""), content.trim)
      }
      .map {
        tuple =>
          val sdf = new SimpleDateFormat("yyyy-MM-dd")
          val s = splitHeaderContent(tuple._2)
          val header = parseMdTitle(s._1)
          val publishDateTime = sdf.parse(header.getOrElse("date", sdf.format(System.currentTimeMillis()))).getTime
          val categories = header.getOrElse("categories", "").trim
          val tags = header.getOrElse("tags", "").trim
          var cs = Seq(Category(defaultSerialNumber, defaultTag))
          var ts = Seq(Tag(defaultSerialNumber, defaultTag))
          val mdMap = parseMd2Html(s._2)
          if (categories.nonEmpty) {
            cs = categories.split(",").map {
              e =>
                Category(defaultSerialNumber, e)
            }.toSeq
          }
          if (tags.nonEmpty) {
            ts = tags.split(",")
              .map {
                e =>
                  Tag(defaultSerialNumber, e)
              }.toSeq
          }
          val title = header.getOrElse("title", tuple._1)
          val author = header.getOrElse("author", "konchoo")
          log.debug("title : " + title)
          log.debug("author : " + author)
          log.debug("content : " + s._2)
          log.debug("categories : " + categories)
          log.debug("tags : " + tags)
          Archive(SerialNumber(0),
            title,
            author,
            new Date(publishDateTime),
            mdMap.get("content").orNull,
            new Date(System.currentTimeMillis()),
            cs,
            ts,
            mdMap.get("toc").orNull
          )
      }.toSeq
    archives.map {
      archive =>
        archiveHandler.createArchive(archive)
    }
  }

  def parseMdTitle(content: String): Map[String, String] = {
    if (content.nonEmpty && content.indexOf("---") != content.lastIndexOf("---")) {
      val keyPairPattern = "\\w+?:\\s*.*\\n"
      val matchContent = content.replaceAll("---", "").replaceAll(" ", "")
      var result: Map[String, String] = Map()
      val it = keyPairPattern.r.findAllIn(matchContent)
      it.foreach {
        i =>
          if (i.nonEmpty && i.contains(":") && i.contains("[") && i.contains("]")) {
            val key = i.split(":")(0)
            val value = i.split(":")(1).replaceAll("(\n)|(\t)", "").replaceAll("(\\[)|]", "")
            result = result + (key -> value)
          }
          else if (i.nonEmpty && i.contains(":")) {
            result = result + (i.split(":")(0) -> i.split(":")(1).replaceAll("\n", ""))
          }
      }
      result
    }
    else Map()
  }

  def splitHeaderContent(s: String): (String, String) = {
    val ss = s.trim
    if (!ss.startsWith("---") || ss.length < 7)
      ("", ss)
    else {
      val sss = ss.drop(3)
      val endIndex = sss.indexOf("---") + 6
      val hexoHeader = ss.substring(0, endIndex)
      val content = ss.drop(endIndex)
      (hexoHeader, content)
    }
  }

  def parseMd2Html(mdContent: String): Map[String, String] = {
    val options = new MutableDataSet()
    val allOptions = PegdownOptionsAdapter.flexmarkOptions(PegdownExtensions.ALL).toMutable
    options.setAll(allOptions)
    val map: util.HashMap[String, String] = new util.HashMap[String, String]()
    map.put("mysql", "language-sql")
    map.put("sql", "language-sql")
    options.set(HtmlRenderer.SOFT_BREAK, "<br />\n")
    options.set(new DataKey[Boolean]("GENERATE_HEADER_ID", true), true)
    options.set(new DataKey[Boolean]("RENDER_HEADER_ID", true), true)
    options.set(HtmlRenderer.FENCED_CODE_LANGUAGE_CLASS_MAP, map)
    val parser = Parser.builder(options).build()
    val htmlRender = HtmlRenderer.builder(options).build()
    val mdDoc = parser.parse(mdContent)
    val mdHtml = htmlRender.render(mdDoc)
    val tocHtml = CatalogUtil.parseHtml2Catalog(mdHtml, 3)
    Map("content" -> mdHtml, "toc" -> tocHtml)
  }
}

class MyMdRead2BaseTask @Inject()(actorSystem: ActorSystem, mdReader: MdReader)(implicit ec: DataBaseExecuteContext) {
  actorSystem.scheduler
    .scheduleOnce(5.seconds, mdReader)
}

trait MdReader extends Runnable {
  def readMd(): Unit
}

class CodeHtml(a: DataHolder) extends IndentedCodeBlockParser(a) {

}
