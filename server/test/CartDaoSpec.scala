import dao.CartDao
import models.{Cart, ProductInCart}
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CartDaoSpec extends PlaySpec with ScalaFutures with GuiceOneAppPerSuite {

  "CartDao" should {
    val app2dao = Application.instanceCache[CartDao]
    "be empty on database creation" in {
      val dao: CartDao = app2dao(app)
      val expected = Set.empty[Cart]

      dao.all().futureValue should contain theSameElementsAs (expected)

    }

    "accept to add new cart" in {
      val dao: CartDao = app2dao(app)
      val user = "userAdd"

      val expected = Set(
        Cart(user, "ALD1", 1),
        Cart(user, "BEO1", 5)
      )
      val noise = Set(
        Cart("userNoise", "ALD2", 10)
      )

      val insertFutures = noise.map(dao.insert(_)) ++ expected.map(dao.insert(_))

      whenReady(Future.sequence(insertFutures)) { _ =>
        dao.cart4(user).futureValue should contain theSameElementsAs (expected)
        dao.all().futureValue.size should equal(expected ++ noise size)
      }
    }

    "accept to remove a product in a cart" in {
      val dao: CartDao = app2dao(app)
      val user = "userRmv"
      val initial = Vector(
        Cart(user, "ALD1", 1),
        Cart(user, "BEO1", 5)
      )
      val expected = Vector(Cart(user, "ALD1", 1))

      whenReady(Future.sequence(initial.map(dao.insert(_)))) { _ =>
        dao.remove(ProductInCart(user, "BEO1")).futureValue
        dao.cart4(user).futureValue should contain theSameElementsAs (expected)
      }
    }

    "accept to update quantities of an item in a cart" in {
      val dao: CartDao = app2dao(app)
      val user = "userUpd"
      val initial = Vector(Cart(user, "ALD1", 1))
      val expected = Vector(Cart(user, "ALD1", 5))

      whenReady(Future.sequence(initial.map(dao.insert(_)))) { _ =>
        dao.update(Cart(user, "ALD1", 5)).futureValue
        dao.cart4(user).futureValue should contain theSameElementsAs (expected)
      }
    }
  }
}
