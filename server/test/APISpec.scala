import models.Cart
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.ws.{DefaultWSCookie, WSClient}
import play.api.test.Helpers._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

import io.circe.parser._
import io.circe.generic.auto._

class APISpec extends PlaySpec with GuiceOneServerPerSuite {

  val baseURL = s"localhost:$port/v1"
  val productsURL = s"http://$baseURL/products"
  val addProductsURL = s"http://$baseURL/products/add"
  val productsInCartURL = s"http://$baseURL/cart/products"

  def deleteProductInCartURL(productID: String) = s"http://$baseURL/cart/products/$productID"

  def actionProductInCartURL(productID: String, quantity: Int) = s"http://$baseURL/cart/products/$productID/quantity/$quantity"

  val login = s"http://$baseURL/login"


  "The API" should {
    val wsClient = app.injector.instanceOf[WSClient]


    "list all the product" in {
      val response = Await.result(wsClient.url(productsURL).get(), 1 seconds)
      println(response.body)
      response.status mustBe OK
      response.body must include("PEPER")
      response.body must include("NAO")
      response.body must include("BEOBOT")
    }

    "add a product" in {

      val newProduct =
        """
                    {
                         "name" : "NewOne",
                         "code" : "New",
                         "description" : "The brand new product",
                         "price" : 100.0
                    }
      """

      val posted = Await.result(wsClient.url(addProductsURL).post(newProduct), 1 seconds)
      posted.status mustBe OK

      val response = Await.result(wsClient.url(productsURL).get(), 1 seconds)
      println(response.body)
      response.body must include("NewOne")
    }

    lazy val defaultCookie = {
      val loginCookies = Await.result(wsClient.url(login).post("me").map(p => p.headers.get("Set-Cookie").map(_.head.split(";").head)), 1 seconds)
      val play_session = loginCookies.get.split("=").tail.mkString("")

      DefaultWSCookie("PLAY_SESSION", play_session)
    }

    "list all the products in a cart" in {
      val response = Await.result(wsClient.url(productsInCartURL).addCookies(defaultCookie).get(), 1 seconds)
      println(response)
      response.status mustBe OK

      val listOfProduct = decode[Seq[Cart]](response.body)
      listOfProduct.right.get mustBe empty
    }
    "add product in the cart" in {
       val productID = "ALD1"
      val quantity = 1
      val posted = Await.result(wsClient.url(actionProductInCartURL(productID, quantity)).addCookies(defaultCookie).post(""), 1 seconds)
      posted.status mustBe OK

      val response = Await.result(wsClient.url(productsInCartURL).addCookies(defaultCookie).get(), 1 seconds)
      println(response)
      response.status mustBe OK
      response.body must include("ALD1")
    }
    "delete product in the cart" in {
      val productID = "ALD1"
      val posted = Await.result(wsClient.url(deleteProductInCartURL(productID)).addCookies(defaultCookie).delete(), 1 seconds)
      posted.status mustBe OK

      val response = Await.result(wsClient.url(productsInCartURL).addCookies(defaultCookie).get(), 1 seconds)
      println(response)
      response.status mustBe OK
      response.body mustNot include("ALD1")
    }
    "update product quantity in the cart" in {
      val productID = "ALD1"
      val quantity = 1
      val posted = Await.result(wsClient.url(actionProductInCartURL(productID, quantity)).addCookies(defaultCookie).post(""), 1 seconds)
      posted.status mustBe OK

      val newQuantity = 99
      val update = Await.result(wsClient.url(actionProductInCartURL(productID, newQuantity)).addCookies(defaultCookie).put(""), 1 seconds)
      update.status mustBe OK

      val response = Await.result(wsClient.url(productsInCartURL).addCookies(defaultCookie).get(), 1 seconds)
      println(response)
      response.status mustBe OK
      response.body must include(productID)
      response.body must include(newQuantity.toString)
    }

    "return a cookie when a user login" in {
      val cookieFuture = wsClient.url(login).post("myID").map { response =>
        response.headers.get("Set-Cookie").map( header =>
          header.head.split(";").filter(_.startsWith("PLAY_SESSION")).head
        )
      }
      val loginCookies = Await.result(cookieFuture, 1 seconds)
      val play_session_Key = loginCookies.get.split("=").head
      play_session_Key must equal("PLAY_SESSION")
    }
  }

}
