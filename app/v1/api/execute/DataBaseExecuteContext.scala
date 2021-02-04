package v1.api.execute

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext

import javax.inject.Inject

class DataBaseExecuteContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "database.context") {

}
