package io.fscala.shopping.client


import java.net.URLEncoder

import io.circe.generic.auto._
import io.circe.parser._
import io.fscala.shopping.shared.{Cart, CartEvent, Product}
import org.querki.jquery._
import org.scalajs.dom
import org.scalajs.dom.html.Document
import org.scalajs.dom.raw.{CloseEvent, Event, MessageEvent, WebSocket}

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.{JSGlobal, ScalaJSDefined}
import scala.util.Try
import scala.scalajs.js.UndefOr.any2undefOrA


object UIManager {

  val origin: UndefOr[String] = dom.document.location.origin
  val cart: CartDiv = CartDiv(Set.empty[CartLine])

  val webSocket = new WebSocket(getWebsocketUri(dom.document, "cart/events"))


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
          seq.foreach(p => {
            $("#products").append(ProductDiv(p).content)
          })
          initCartUI(origin, seq)
        }
      })
      .fail((xhr: JQueryXHR, textStatus: String, textError: String) =>
        println(s"call failed: $textStatus with status code: ${xhr.status} $textError")
      )


  }

  @js.native
  @JSGlobal("$")
  object NotifyJS extends js.Object {
    def notify(msg: String, option: Options): Nothing = js.native

    def notify(msg: String, className: String): Nothing = js.native
  }

  @ScalaJSDefined
  trait Options extends js.Object {
    // whether to hide the notification on click
    val clickToHide: js.UndefOr[Boolean]= js.undefined
    // whether to auto-hide the notification
    val autoHide: js.UndefOr[Boolean]= js.undefined
    // if autoHide, hide after milliseconds
    val autoHideDelay: js.UndefOr[Int] = js.undefined
    // show the arrow pointing at the element
    val arrowShow: js.UndefOr[Boolean]= js.undefined
    // arrow size in pixels
    val arrowSize: js.UndefOr[Int] = js.undefined
    // position defines the notification position though uses the defaults below
    val position: js.UndefOr[String] = js.undefined
    // default positions
    val elementPosition: js.UndefOr[String] = js.undefined
    val globalPosition: js.UndefOr[String] = js.undefined
    // default style
    val style: js.UndefOr[String] = js.undefined
    // default class (string or [string])
    val className: js.UndefOr[String] = js.undefined
    // show animation
    val showAnimation: js.UndefOr[String] = js.undefined
    // show animation duration
    val showDuration: js.UndefOr[Int] = js.undefined
    // hide animation
    val hideAnimation: js.UndefOr[String] = js.undefined
    // hide animation duration
    val hideDuration: js.UndefOr[Int] = js.undefined
    // padding between element and notification
    val gap: js.UndefOr[Int] = js.undefined
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

      NotifyJS.notify(s"product '${product.code}' added", new Options { override val className = "info"} )
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
      NotifyJS.notify(s"product '$productCode' removed", "warn")
      println(s"Product $productCode removed from the cart")
    }

    deletefromCart(productCode, onDone)
  }

  private def getWebSocket: WebSocket = {
    webSocket.onopen = { (event: Event) ⇒
      println(s"webSocket.onOpen '${event.`type`}'")
      event.preventDefault()
    }

    webSocket.onerror = { (event: Event) =>
      System.err.println(s"webSocket.onError '${event.getClass}'")
    }

    webSocket.onmessage = { (event: MessageEvent) =>
      println(s"[webSocket.onMessage] '${URLEncoder.encode(event.data.toString, "UTF-8")}'...")
      val msg = decode[CartEvent](event.data.toString)
      msg match {
        case Right(cartEvent) =>
          println(s"[webSocket.onMessage]  Got cart event : $cartEvent)")
        //TODO SPWAN a bootstrap notification

        case Left(e) =>
          println(s"[webSocket.onMessage] Got a unknown event : $msg)")
      }
    }

    webSocket.onclose = { (event: CloseEvent) ⇒
      println(s"webSocket.onClose '${event.`type`}'")
    }
    webSocket
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

  private def getWebsocketUri(document: Document, context: String): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"

    s"$wsProtocol://${
      dom.document.location.host
    }/$context"
  }

}
