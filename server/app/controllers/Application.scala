package controllers

import actors.{BrowserActor, UserActor}
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import javax.inject._
import dao.ProductDao
import io.fscala.shopping.shared.WebsocketMessage
import play.api.http.websocket._
import play.api.libs.circe.Circe
import play.api.libs.streams.{ActorFlow, AkkaStreams}
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc._
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import play.api.Logger

import scala.util.control.NonFatal



@Singleton
class Application @Inject()(implicit actorSystem: ActorSystem, materializer: Materializer, cc: ControllerComponents, productDao: ProductDao) extends AbstractController(cc) with Circe {

  val browserActor = actorSystem.actorOf(BrowserActor.props(), "browser-actor")

  def index = Action {
    Ok(views.html.index("Shopping Page"))
  }

  def cartEvent = Action {
    NotImplemented
  }

  def cartEventWS = WebSocket.accept[String, String] { implicit request =>
    ActorFlow.actorRef{out =>
      Logger.info(s"Got a new websocket connection from ${request.host}")
      browserActor ! BrowserActor.AddBrowser(out)
      UserActor.props(out, browserActor)
    }
  }
}
