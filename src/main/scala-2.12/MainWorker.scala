import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import StaticObjects._
import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonString}
import org.mongodb.scala.{Completed, Observer}
import spray.json._

import scala.concurrent.duration._
import scala.io.Source
import scala.util.Random

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

      val googleConfig = googleConfigJson.asJsObject.fields.map({
        case (key : String, JsString(value : String)) => (key, value)
      })

      val paths = pathsJson.asJsObject.fields.map({
        case (key : String, JsString(value : String)) => (key, value)
      })

      val proxiesPath = paths("proxiesPath")
      val searchRequestsPath = paths("searchRequestsPath")

      val proxies = Source.fromFile(new File(proxiesPath)).getLines().toList

      val proxiesIterator = new ProxiesIterator(proxies)

      val linesIterator = Source.fromFile(new File(searchRequestsPath)).getLines()

      if (linesIterator.hasNext)
        self ! MakeGoogleRequest(linesIterator, proxiesIterator, googleConfig)

    case MakeGoogleRequest(searchStringIterator, proxiesIterator, googleConfig) =>
      val searchString = searchStringIterator.next()

      val proxy = proxiesIterator.next()
      val (host, port) = {
        val s = proxy.split(":")
        (s(0), s(1).toInt)
      }

      //val uri = Uri.from(scheme = "http", host = host, port = port, path = buildGoogleRequest(searchString, googleConfig))
      val uri = buildGoogleRequest(searchString, googleConfig)

      http.singleRequest(HttpRequest(uri = uri)).map(self ! (_, searchString))

      if (searchStringIterator.hasNext)
        system.scheduler.scheduleOnce(200 + new Random().nextInt(300) milliseconds,
          self, MakeGoogleRequest(searchStringIterator, proxiesIterator, googleConfig))

    case (HttpResponse(StatusCodes.OK, _, entity, _), searchString : String) =>
      val byteStringFuture = entity.toStrict(2 second)

      byteStringFuture.map(strict => {
        val string = strict.data.utf8String

        val result = GooglePageParser.parse(string)

        self ! SaveResultInDatabase(result, searchString)
      })

    case SaveResultInDatabase(result : List[String], searchString : String) =>
      val dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
      val date = new Date()
      val currentDate = dateFormat.format(date)

      val doc = BsonDocument("searchString" -> searchString, "time" -> BsonString(currentDate), "result" -> BsonArray(
        result.map(BsonString(_))
      ))

      dbCollection.insertOne(doc).subscribe(new Observer[Completed] {
        override def onError(e: Throwable): Unit = println(e)
        override def onComplete(): Unit = println("Success")
        override def onNext(result: Completed): Unit = {}
      })
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
