import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by vlad on 16.02.17.
  */
object StaticObjects {

  implicit val system = ActorSystem("actor-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val mainWorker: ActorRef = system.actorOf(Props[MainWorker])

  val mongoDbClient = MongoClient()
  val database: MongoDatabase = mongoDbClient.getDatabase("database")
  val dbCollection: MongoCollection[Document] = database.getCollection("googleParser")

  case class Request(entity : String)
  case class MakeGoogleRequest(searchStringIterator : Iterator[String], proxiesInterator : ProxiesIterator, googleConfig : Map[String, String])
  case class SaveResultInDatabase(result : List[String])

  class ProxiesIterator(private val proxies : List[String]) {

    private var i = 0

    def next() : String = {
      if (i == proxies.length)
        i = 0
      i += 1
      proxies(i - 1)
    }

  }

}
