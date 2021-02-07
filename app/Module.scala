import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}
import v1.api.repository._

/**
  * Sets up custom components for Play.
  *
  * https://www.playframework.com/documentation/latest/ScalaDependencyInjection
  */
class Module(environment: Environment, configuration: Configuration)
  extends AbstractModule
    with ScalaModule {

  override def configure(): Unit = {
    bind[ArchiveRepository].to[ArchiveRepositoryImpl]
    bind[CategoryRepository].to[CategoryRepositoryImpl]
    bind[TagRepository].to[TagRepositoryImpl]
    bind[ArchiveCategoryRepository].to[ArchiveCategoryRepositoryImpl]
    bind[ArchiveTagRepository].to[ArchiveTagRepositoryImpl]
  }
}
