import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer

/**
  * Created by vlad on 16.02.17.
  */
object StaticObjects {

  implicit val system = ActorSystem("actor-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val mainWorker = system.actorOf(Props[MainWorker])

}
