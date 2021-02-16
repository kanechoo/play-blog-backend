package support.hexo

import akka.actor.ActorSystem
import com.google.inject.{Inject, Singleton}
import play.api.Logger
import v1.api.cont.Default._
import v1.api.entity.{Archive, Category, SerialNumber, Tag}
import v1.api.execute.DataBaseExecuteContext
import v1.api.handler.ArchiveHandler

import java.io.File
import java.sql.Date
import java.text.SimpleDateFormat
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
          (file.getName, content.trim)
      }
      .map {
        tuple =>
          val exceptHeaderContent = tuple._2.substring(tuple._2.lastIndexOf("---") + 3)
          val sdf = new SimpleDateFormat("yyyy-MM-dd")
          val header = parseMdTitle(tuple._2)
          val publishDateTime = sdf.parse(header.getOrElse("date", sdf.format(System.currentTimeMillis()))).getTime
          var categories = header.getOrElse("categories", "").split(",")
            .map {
              e =>
                Category(SerialNumber(0), e)
            }.toSeq
          var tags = header.getOrElse("tags", "").split(",")
            .map {
              e =>
                Tag(SerialNumber(0), e)
            }.toSeq
          if (null == categories || categories.isEmpty) categories = Seq(Category(defaultSerialNumber, defaultCategory))
          if (null == tags || tags.isEmpty) tags = Seq(Tag(defaultSerialNumber, defaultTag))
          val title = header.getOrElse("title", tuple._1)
          val author = header.getOrElse("author", "konc")
          log.debug("title : " + title)
          log.debug("author : " + author)
          log.debug("content : " + exceptHeaderContent)
          log.debug("categories : " + categories)
          log.debug("tags : " + tags)
          Archive(SerialNumber(0),
            title,
            author,
            new Date(publishDateTime),
            exceptHeaderContent,
            new Date(System.currentTimeMillis()),
            categories,
            tags
          )
      }.toSeq
    archives.map {
      archive =>
        archiveHandler.createArchive(archive)
    }
  }

  def parseMdTitle(content: String): Map[String, String] = {
    if (content.nonEmpty && content.startsWith("---")) {
      val keyPairPattern = "\\w+?:\\s*.*\\n"
      val end = content.lastIndexOf("---")
      val matchContent = content.substring(0, end + 3).replaceAll(" ", "")
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
}

class MyMdRead2BaseTask @Inject()(actorSystem: ActorSystem, mdReader: MdReader)(implicit ec: DataBaseExecuteContext) {
  actorSystem.scheduler
    .scheduleOnce(5.seconds, mdReader)
}

trait MdReader extends Runnable {
  def readMd(): Unit
}
