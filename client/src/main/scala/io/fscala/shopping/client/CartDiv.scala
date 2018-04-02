package io.fscala.shopping.client

import scalatags.JsDom.all._

case class CartDiv(lines: Set[CartLine]) {
  def content = lines.foldLeft(div.render) { (a, b) =>
    a.appendChild(b.content).render
    a
  }

  def addProduct(line: CartLine): CartDiv = {
    new CartDiv(this.lines + line)
  }

}

case class CartLine(qty: Int, productName: String, productCode: String, price: Double) {
  def content = div(`class` := "row", id := s"$productCode-row")(
    div(`class` := "col-1")(deleteButton),
    div(`class` := "col-2")(quantityInput),
    div(`class` := "col-6")(productLabel),
    div(`class` := "col")(priceLabel)
  ).render

  private def quantityInput = input(id := s"cart-$productCode-qty", onchange := changeQty, value := qty.toString, `type` := "text", style := "width: 100%;").render

  private def productLabel = label(productName).render

  private def priceLabel = label(price * qty).render


  private def deleteButton = button(`type` := "button", onclick := removeFromCart)("X").render

  private def changeQty = () => UIManager.manager.updateProduct(productCode)

  private def removeFromCart = () => UIManager.manager.deleteProduct(productCode)
}