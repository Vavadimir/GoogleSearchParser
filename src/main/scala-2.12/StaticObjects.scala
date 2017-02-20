import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

import scala.concurrent.ExecutionContextExecutor

import scala.concurrent.duration._

/**
  * Created by vlad on 16.02.17.
  */
object StaticObjects {

  implicit val system = ActorSystem("actor-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(3 seconds)

  val mainWorker: ActorRef = system.actorOf(Props[MainWorker])

  var progressMessagePublisher : ActorRef = _

  val mongoDbClient = MongoClient()
  val database: MongoDatabase = mongoDbClient.getDatabase("local")
  val dbCollection: MongoCollection[Document] = database.getCollection("GoogleParserCollection")

  case class Request(entity : String)
  case class MakeGoogleRequest(searchStringIterator : Iterator[String], proxiesIterator : ProxiesIterator, googleConfig : Map[String, String])
  case class SaveResultInDatabase(result : List[String], searchString : String)
  case class GetFileLength(filePath : String)

  class ProxiesIterator(private val proxies : Array[String]) {

    private var i = 0

    def next() : String = {
      if (i == proxies.length)
        i = 0
      i += 1
      proxies(i - 1)
    }

  }

}
