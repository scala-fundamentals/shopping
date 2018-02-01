import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.ws.WSClient

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

//import org.scalatest._
//import org.scalatestplus.play._

//import play.api.test._
import play.api.test.Helpers.{GET => GET_REQUEST, _}

class APISpec extends PlaySpec with GuiceOneServerPerSuite {

  "The API" should {
    val wsClient = app.injector.instanceOf[WSClient]
    val myPublicAddress = s"localhost:$port"
    "list all the product" in {
      val testURL = s"http://$myPublicAddress/products"

      val response = Await.result(wsClient.url(testURL).get(), 1 seconds)
      println(response.body)
      response.status mustBe OK
    }
    "list all the product in a cart" in {
      val testURL = s"http://$myPublicAddress/cart/products"

      val response = Await.result(wsClient.url(testURL).get(), 1 seconds)
      response.status mustBe OK
    }
  }

}
