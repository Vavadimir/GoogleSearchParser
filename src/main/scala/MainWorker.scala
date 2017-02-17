import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes, Uri}
import spray.json._

import scala.io.Source
import scala.util.Random
import StaticObjects._

import scala.concurrent.duration._

/**
  * Created by vlad on 16.02.17.
  */
class MainWorker extends Actor {

  private val http = Http(context.system)

  override def receive: Receive = {
    case Request(entity : String) =>
      val json = entity.parseJson
      val fields = json.asJsObject.fields

      val googleConfigJson = fields("googleConfig")
      val pathsJson = fields("paths")

      val googleConfig = googleConfigJson.asJsObject.fields.map(i => (i._1, i._2.toString()))
      val paths = pathsJson.asJsObject.fields.map(i => (i._1, i._2.toString()))

      val proxiesPath = paths("proxiesPath")
      val searchRequestsPath = paths("searchRequestsPath")

      val proxies = Source.fromFile(proxiesPath).getLines().toList

      val proxiesIterator = new ProxiesIterator(proxies)

      val linesIterator = Source.fromFile(searchRequestsPath).getLines()

      if (linesIterator.hasNext)
        self ! MakeGoogleRequest(linesIterator, proxiesIterator, googleConfig)

    case MakeGoogleRequest(searchStringIterator, proxiesIterator, googleConfig) =>
      val searchString = searchStringIterator.next()

      val proxy = proxiesIterator.next()
      val (host, port) = {
        val s = proxy.split(":")
        (s(0), s(1).toInt)
      }

      val uri = Uri.from(scheme = "http", host = host, port = port, path = buildGoogleRequest(searchString, googleConfig))

      http.singleRequest(HttpRequest(uri = uri)).map(self ! _)

      if (searchStringIterator.hasNext)
        system.scheduler.scheduleOnce(200 + new Random().nextInt(300) milliseconds,
          self, MakeGoogleRequest(searchStringIterator, proxiesIterator, googleConfig))

    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      //Parse page
      val result = List("someSite")
      //

      self ! SaveResultInDatabase(result)

    case SaveResultInDatabase(result : List[String]) =>
      //To be done
  }

  private def buildGoogleRequest(searchQuery : String, config : Map[String, String]) : String = {
    val stringBuilder = new StringBuilder("https://www.google.com.ua/search?q=" + searchQuery + "&num=100")

    config.foreach(i => {
      val (key, value) = i
      stringBuilder.append(s"&$key=$value")
    })

    stringBuilder.toString()
  }
}
