package dao

import javax.inject.Inject

import models.{Cart, Product}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


class ProductsDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def all(): Future[Seq[Product]] = db.run(Products.result)

  def insert(product: Product): Future[Unit] = db.run(Products += product).map { _ => () }

  private class ProductsTable(tag: Tag) extends Table[Product](tag, "PRODUCTS") {
    def name = column[String]("NAME")

    def code = column[String]("CODE")

    def description = column[String]("DESCRIPTION")

    def price = column[Double]("PRICE")

    override def * = (name, code, description, price) <> (Product.tupled, Product.unapply)
  }

  private val Products = TableQuery[ProductsTable]
}

class CartsDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._


  def cart4(usr : String): Future[Seq[Cart]] = db.run(Carts.filter(_.user === usr).result)

  def insert(cart: Cart): Future[Unit] = db.run(Carts += cart).map { _ => () }

  def all(): Future[Seq[Cart]] = db.run(Carts.result)

  private class CartsTable(tag: Tag) extends Table[Cart](tag, "CART") {


    def user = column[String]("USER")

    def productCode = column[String]("CODE")

    def quantity = column[Int]("QTY")

    override def * = (user, productCode, quantity) <> (Cart.tupled, Cart.unapply)
  }


  private val Carts = TableQuery[CartsTable]


}


