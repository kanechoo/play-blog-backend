package task

import play.api.inject.{SimpleModule, _}
import support.hexo.MyMdRead2BaseTask

class TaskModule extends SimpleModule(bind[MyMdRead2BaseTask].toSelf.eagerly())