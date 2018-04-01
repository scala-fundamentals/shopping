package controllers

import javax.inject._

import dao.ProductDao
import io.fscala.shopping.shared.SharedMessages
import play.api.libs.circe.Circe
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global


@Singleton
class Application @Inject()(cc: ControllerComponents, productDao: ProductDao) extends AbstractController(cc) with Circe {

  def index = Action {
    Ok(views.html.index("Shopping Page"))
  }

}
