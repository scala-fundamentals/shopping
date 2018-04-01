package io.fscala.shopping.client

import io.fscala.shopping.shared.Product
import org.scalajs.dom

object UIManager {

  def main(args: Array[String]): Unit = {
    println("Executing Main ...")
    val manager = new UIManager()
    dom.document.getElementById("products").appendChild(ProductDiv(Product("product name 1", "P1", "The description of the product 1", 99.99), manager).content)
    dom.document.getElementById("products").appendChild(ProductDiv(Product("product name 2", "P2", "The description of the product 2", 9.99), manager).content)
    dom.document.getElementById("products").appendChild(ProductDiv(Product("product name 3", "P3", "The description of the product 3", 9.99), manager).content)
  }
}

case class UIManager(cart: CartDiv = CartDiv(Set.empty[CartLine])) {
  def addProduct(product: Product) = {
    //dom.document.getElementById("cartPanel").innerHTML = ""
    val cartContent = cart.addProduct(CartLine(1,product.name, product.code,product.price)).content
    dom.document.getElementById("cartPanel").appendChild(cartContent)
    cart
  }
}
