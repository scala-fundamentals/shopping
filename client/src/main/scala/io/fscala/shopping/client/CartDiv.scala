package io.fscala.shopping.client

import org.scalajs.dom.html.Div
import scalatags.JsDom.all._

case class CartDiv(lines: Set[CartLine]) {
  def content: Div = lines.foldLeft(div.render) { (a, b) =>
    a.appendChild(b.content).render
    a
  }

  def addProduct(line: CartLine): CartDiv = {
    CartDiv(this.lines + line)
  }

}

case class CartLine(qty: Int, productName: String, productCode: String, price: Double) {
  def content: Div = div(`class` := "row", id := s"cart-$productCode-row")(
    div(`class` := "col-1")(getDeleteButton),
    div(`class` := "col-2")(getQuantityInput),
    div(`class` := "col-6")(getProductLabel),
    div(`class` := "col")(getPriceLabel)
  ).render

  private def getQuantityInput = input(id := s"cart-$productCode-qty", onchange := changeQty, value := qty.toString, `type` := "text", style := "width: 100%;").render

  private def getProductLabel = label(productName).render

  private def getPriceLabel = label(price * qty).render

  private def getDeleteButton = button(`type` := "button", onclick := removeFromCart)("X").render

  private def changeQty() = () => UIManager.updateProduct(productCode)

  private def removeFromCart() = () => UIManager.deleteProduct(productCode)
}