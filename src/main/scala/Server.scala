import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import scala.io.StdIn

import StaticObjects._

/**
  * Created by vlad on 16.02.17.
  */
object Server extends App {

  val route = {
    path("parseGoogle") {
      post {
        entity(as[String]) {
          entity => {
            StaticObjects.mainWorker ! entity

            complete("Ok. Working")
          }
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
