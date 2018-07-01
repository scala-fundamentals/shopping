package io.fscala.shopping.client


import io.circe.generic.auto._, io.circe.parser._
import io.fscala.shopping.shared.{Cart, Product}
import org.querki.jquery._
import org.scalajs.dom

import scala.scalajs.js.UndefOr
import scala.util.Try

object UIManager {

  val origin: UndefOr[String] = dom.document.location.origin
  val cart: CartDiv = CartDiv(Set.empty[CartLine])

  def main(args: Array[String]): Unit = {
    val settings = JQueryAjaxSettings.url(s"$origin/v1/login").data("theUser").contentType("text/plain")
    $.post(settings._result).done((_: String) => {
      initUI(origin)
    })
  }

  private def initUI(origin: UndefOr[String]) = {
    $.get(url = s"$origin/v1/products", dataType = "text")
      .done((answers: String) => {
        val products = decode[Seq[Product]](answers)
        products.right.map { seq =>
          seq.foreach(p =>
            $("#products").append(ProductDiv(p).content)
          )
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
        carts.right.map { cartLines =>
          cartLines.foreach { cartDao =>
            val product = products.find(_.code == cartDao.productCode)
            product match {
              case Some(p) =>
                val cartLine = CartLine(cartDao.quantity, p.name, cartDao.productCode, p.price)
                val cartContent = UIManager.cart.addProduct(cartLine).content
                $("#cartPanel").append(cartContent)
              case None =>
                println(s"product code ${cartDao.productCode} doesn't exists in the catalog")
            }
          }
        }
      })
      .fail((xhr: JQueryXHR, textStatus: String, textError: String) =>
        println(s"call failed: $textStatus with status code: ${xhr.status} $textError")
      )
  }

  def addOneProduct(product: Product): JQueryDeferred = {
    val quantity = 1

    def onDone = () => {
      val cartContent = cart.addProduct(CartLine(quantity, product.name, product.code, product.price)).content
      $("#cartPanel").append(cartContent)
      println(s"Product $product added in the cart")
    }

    postInCart(product.code, quantity, onDone)
  }

  def updateProduct(productCode: String): JQueryDeferred = {
    putInCart(productCode, quantity(productCode))
  }

  def deleteProduct(productCode: String): JQueryDeferred = {
    def onDone = () => {
      val cartContent = $(s"#cart-$productCode-row")
      cartContent.remove()
      println(s"Product $productCode removed from the cart")
    }

    deletefromCart(productCode, onDone)
  }

  private def quantity(productCode: String) = Try {
    val inputText = $(s"#cart-$productCode-qty")
    if (inputText.length != 0) Integer.parseInt(inputText.`val`().asInstanceOf[String]) else 1
  }.getOrElse(1)

  private def postInCart(productCode: String, quantity: Int, onDone: () => Unit) = {
    val url = s"${UIManager.origin}/v1/cart/products/$productCode/quantity/$quantity"
    $.post(JQueryAjaxSettings.url(url)._result)
      .done(onDone)
      .fail(() => println("cannot add a product twice"))
  }

  private def putInCart(productCode: String, updatedQuantity: Int) = {
    val url = s"${UIManager.origin}/v1/cart/products/$productCode/quantity/$updatedQuantity"
    $.ajax(JQueryAjaxSettings.url(url).method("PUT")._result)
      .done()
  }

  private def deletefromCart(productCode: String, onDone: () => Unit) = {
    val url = s"${UIManager.origin}/v1/cart/products/$productCode"
    $.ajax(JQueryAjaxSettings.url(url).method("DELETE")._result)
      .done(onDone)
  }
}
