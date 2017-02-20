import StaticObjects._
import akka.actor.Props
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.UpgradeToWebSocket
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExpectedWebSocketRequestRejection
import akka.stream.scaladsl.{Sink, Source}

import scala.io.StdIn

/**
  * Created by vlad on 16.02.17.
  */
object Server extends App {

  val route = {
    path("parseGoogle") {
      post {
        entity(as[String]) {
          entity => {
            StaticObjects.mainWorker ! Request(entity)

            complete("Ok. Working")
          }
        }
      }
    } ~ {
      path("progress") {
        optionalHeaderValueByType[UpgradeToWebSocket]() {
          case Some(upgrade) =>
            complete {
              upgrade.handleMessagesWithSinkSource(
                Sink.ignore,
                Source.actorPublisher(Props[ProgressMessagePublisher])
              )
            }
          case None => reject(ExpectedWebSocketRequestRejection)
        }
      }
    }
  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 9999)

  println(s"Server online at http://localhost:9999/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
