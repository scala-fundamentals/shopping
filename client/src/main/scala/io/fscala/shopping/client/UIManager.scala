package io.fscala.shopping.client

import io.circe.generic.auto._
import io.circe.parser._
import io.fscala.shopping.shared.{Cart, Product}
import org.querki.jquery._
import org.scalajs.dom

import scala.scalajs.js.UndefOr
import scala.util.Try

object UIManager {

  val manager = new UIManager()

  val origin = dom.document.location.origin

  def main(args: Array[String]): Unit = {
    println("Executing Main ...")

    val settings = JQueryAjaxSettings.url(s"$origin/v1/login").data("nicolas11").contentType("text/plain")

    $.post(settings._result).done((answers: String) => {
      println(s"logged in ...")
      initUI(origin)
    })
  }

  private def initUI(origin: UndefOr[String]) = {
    $.get(url = s"$origin/v1/products", dataType = "text")
      .done((answers: String) => {
        val products = decode[Seq[Product]](answers)
        products.right.map { seq =>
          seq.foreach(p => dom.document.getElementById("products").appendChild(ProductDiv(p).content))
          initCartUI(origin, seq)
        }
      })
      .fail((xhr: JQueryXHR, textStatus: String, textError: String) =>
        println(s"call failed: $textStatus with status code: ${xhr.status} $textError")
      )
  }

  private def initCartUI(origin: UndefOr[String], products: Seq[Product]) = {
    $.get(url = s"$origin/v1/cart/products", dataType = "text")
      .done((answers: String) => {
        val carts = decode[Seq[Cart]](answers)
        carts.right.map { carts =>
          carts.foreach { cartDao =>
            val product = products.filter(_.code == cartDao.productCode).headOption
            product match {
              case Some(p) => {
                val cartContent = UIManager.manager.cart.addProduct(CartLine(cartDao.quantity, p.name, cartDao.productCode, p.price)).content
                dom.document.getElementById("cartPanel").appendChild(cartContent)
              }
              case None => println(s"product code ${cartDao.productCode} doesn't exists in the catalog")
            }
          }
        }
      })
      .fail((xhr: JQueryXHR, textStatus: String, textError: String) =>
        println(s"call failed: $textStatus with status code: ${xhr.status} $textError")
      )
  }
}

case class UIManager(cart: CartDiv = CartDiv(Set.empty[CartLine])) {

  def addOneProduct(product: Product) = {
    val updatedQuantity = quantity(product.code) + 1

    def onDone = () => {
      val cartContent = cart.addProduct(CartLine(1, product.name, product.code, product.price)).content
      val node = dom.document.getElementById("cartPanel").appendChild(cartContent)
    }

    postInCart(product.code, updatedQuantity, onDone)
  }

  def updateProduct(productCode: String) = {
    putInCart(productCode, quantity(productCode))
  }

  def deleteProduct(productCode:String) = {
    def onDone = () => {
      val cartContent = $(s"#$productCode-row")
      val result = cartContent.remove()
    }
    deletefromCart(productCode, onDone)
  }

  private def quantity(productCode: String) = Try {
    val inputText = $(s"#cart-$productCode-qty")
    if (inputText.length != 0) Integer.parseInt(inputText.`val`().asInstanceOf[String]) else 0
  }.getOrElse(0)

  private def postInCart(productCode: String, updatedQuantity: Int, onDone: () => Unit = () => ()) = {
    $.post(JQueryAjaxSettings.url(s"${UIManager.origin}/v1/cart/products/$productCode/quantity/${updatedQuantity}")._result)
      .done(onDone)
  }
  private def putInCart(productCode: String, updatedQuantity: Int) = {
    $.post(JQueryAjaxSettings.url(s"${UIManager.origin}/v1/cart/products/$productCode/quantity/${updatedQuantity}")._result)
      .done()
  }

  private def deletefromCart(productCode: String, onDone: () => Unit) = {
    $.ajax(JQueryAjaxSettings.url(s"${UIManager.origin}/v1/cart/products/$productCode").method("DELETE")._result)
      .done(onDone)
  }
}
