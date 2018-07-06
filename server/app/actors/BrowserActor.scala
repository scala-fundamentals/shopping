package actors

import actors.BrowserActor.AddBrowser
import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import io.fscala.shopping.shared._

import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._

import scala.collection.mutable.ListBuffer

object BrowserActor {
  def props() = Props(new BrowserActor())

  case class AddBrowser(browser: ActorRef)

}

private class BrowserActor() extends Actor with ActorLogging {

  var browsers: ListBuffer[ActorRef] = ListBuffer.empty[ActorRef]

  def receive: Receive = {
    case CartEvent(user, product, action) =>
      val messageText = s"The user '${user}' ${action.toString} ${product.name}"
      log.info("Sending alarm to all the browser with '{}' action: {}",messageText, action)
      browsers.foreach(_ ! Alarm(messageText,action).asJson.noSpaces)
    case AddBrowser(b) =>
      context.watch(b)
      browsers = browsers :+ b
      log.info("websocket {} added",b.path)
    case Terminated(b) =>
      browsers -= b
      log.info("websocket {} removed",b.path)
  }
}
