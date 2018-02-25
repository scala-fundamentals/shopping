

import dao.{CartsDao, ProductsDao}
import models.{Cart, Product, ProductInCart}
import org.scalatest.Matchers._
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}


class DatabaseSpec extends PlaySpec with GuiceOneAppPerSuite {
  "ProductsDao" should {
    "Have default rows on database creation" in {
      val app2dao = Application.instanceCache[ProductsDao]
      val dao: ProductsDao = app2dao(app)

      val expected = Set(
        Product("PEPER", "ALD2", "PEPPER is a robot moving with wheels and with a screen as human interaction", 7000),
        Product("NAO", "ALD1", "NAO is an humanoid robot.", 3500),
        Product("BEOBOT", "BEO1", "Beobot is a multipurpose robot.", 159.0)
      )

      val dbResults = Await.result(dao.all(), 1 seconds)

      dbResults.toSet should contain theSameElementsAs (expected)
    }
  }

  "CartsDao" should {
    val app2dao = Application.instanceCache[CartsDao]
    "be empty on database creation" in {
      val dao: CartsDao = app2dao(app)
      val expected = Set.empty[Cart]
      val dbResults = Await.result(dao.all("user1"), 1 seconds)

      dbResults.toSet should contain theSameElementsAs (expected)
    }
    "accept to add new cart" in {
      val dao: CartsDao = app2dao(app)
      val expected = Set(
        Cart("user1", "ALD1", 1),
        Cart("user1", "BEO1", 5)
      )
      val noise = Set(
        Cart("user2", "ALD2", 10)
      )
      Await.result(Future.sequence(expected.map(dao.insert(_))), 1 seconds)
      Await.result(Future.sequence(noise.map(dao.insert(_))), 1 seconds)
      val dbResults = Await.result(dao.cart4("user1"), 1 seconds)
      val dbAllResults = Await.result(dao.all("user1"), 1 seconds)

      dbResults.toSet should contain theSameElementsAs (expected)
      dbAllResults.size should equal(expected ++ noise size)
    }

    "accept to remove a product in a cart" in {
      val dao: CartsDao = app2dao(app)
      val initial = Set(
        Cart("user1", "ALD1", 1),
        Cart("user1", "BEO1", 5)
      )
      val expected = Set(Cart("user1", "ALD1", 1))
      Await.result(Future.sequence(initial.map(dao.insert(_))), 1 seconds)

      Await.result(dao.remove(ProductInCart("user1", "BEO1")), 1 seconds)

      val dbResults = Await.result(dao.cart4("user1"), 1 seconds)
      val dbAllResults = Await.result(dao.all("user1"), 1 seconds)

      dbResults.toSet should contain theSameElementsAs (expected)
    }

    "accept to update quantities of an item in a cart" in {
      val dao: CartsDao = app2dao(app)
      val initial = Set(Cart("user1", "ALD1", 1))
      val expected = Set(Cart("user1", "ALD1", 5))

      Await.result(Future.sequence(initial.map(dao.insert(_))), 1 seconds)

      Await.result(dao.update(Cart("user1", "ALD1",5)), 1 seconds)

      val dbResults = Await.result(dao.cart4("user1"), 1 seconds)
      val dbAllResults = Await.result(dao.all("user1"), 1 seconds)

      dbResults.toSet should contain theSameElementsAs (expected)
    }
  }
}
