import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import spray.json._

import scala.io.Source

/**
  * Created by vlad on 16.02.17.
  */
class MainWorker extends Actor {

  val http = Http(context.system)

  override def receive: Receive = {
    case s : String =>
      val json = s.parseJson
      val fields = json.asJsObject.fields

      val googleConfigJson = fields("googleConfig")
      val pathsJson = fields("paths")

      val googleConfig = googleConfigJson.asJsObject.fields.map(i => (i._1, i._2.toString()))
      val paths = pathsJson.asJsObject.fields.map(i => (i._1, i._2.toString()))

      val proxiesPath = paths("proxiesPath")
      val searchRequestsPath = paths("searchRequestsPath")

      val proxies = Source.fromFile(proxiesPath).getLines().toList

      val proxiesIterator = new ProxiesIterator(proxies)

      for (line <- Source.fromFile(searchRequestsPath).getLines()) {
        val proxy = proxiesIterator.next()

        //Build url
        val url = ""

        http.singleRequest(HttpRequest(uri = url)).map(self ! _)
      }

    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      //Parse page
  }

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
