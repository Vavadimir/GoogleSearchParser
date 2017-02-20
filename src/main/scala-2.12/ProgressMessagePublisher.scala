import StaticObjects._
import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}

import scala.collection.mutable

/**
  * Created by vlad on 20.02.17.
  */
class ProgressMessagePublisher extends ActorPublisher[TextMessage] {

  private val queue = new mutable.Queue[String]()

  override def receive: Receive = {
    case progress : Double =>
      queue.enqueue(progress.toString)
      sendMessages()
    case Request(_) =>
      sendMessages()
    case Cancel =>
      context.stop(self)
    case a => println("Action unsupported. Client " + a)
  }

  override def preStart(): Unit = progressMessagePublisher = self

  def sendMessages(): Unit = {
    while (isActive && totalDemand > 0 && queue.nonEmpty)
      onNext(TextMessage(queue.dequeue()))
  }

}
