package io.fscala.shopping.controllers

import javax.inject._

import dao.ProductsDao
import io.fscala.shopping.shared.SharedMessages
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global



@Singleton
class Application @Inject()(cc: ControllerComponents, productDao : ProductsDao) extends AbstractController(cc) {

  def index = Action {
    Ok(views.html.index(SharedMessages.itWorks))
  }

  // *********** CART Controler ******** //
  def listCartProducts() = play.mvc.Results.TODO

  def deleteCartProduct(id: String) = play.mvc.Results.TODO

  def addCartProduct(id: String, quantity: String) = play.mvc.Results.TODO

  def updateCartProduct(id: String, quantity: String) = play.mvc.Results.TODO

  // *********** Product Controler ******** //
  def listProduct() = Action.async { request =>
    productDao.all().map(s => Ok(s.mkString(" ")))
  }


}
