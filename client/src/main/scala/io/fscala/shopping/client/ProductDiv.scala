package io.fscala.shopping.client

import io.fscala.shopping.shared.Product

import scalatags.JsDom.all._


case class ProductDiv(product: Product, manager : UIManager) {
  def content = div(`class` := "col")(productDescription, addButton).render

  private def productDescription =
      div(
        p(product.name),
        p(product.description),
        p(product.price))


  private def addButton = button(`type` := "button", onclick := addToCart)("Add to Cart")

  private def addToCart = () => manager.addProduct(product)
}
