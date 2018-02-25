import io.circe.generic.auto._
import io.circe.syntax._
import models.Product
import io.circe.parser._

val newProduct = Product("NewOne","New","The brand new product", 100.0)

newProduct.asJson

val json = """{
                |  "name" : "NewOne",
                |  "code" : "New",
                |  "description" : "The brand new product",
                |  "price" : 100.0
                |}""".stripMargin

decode[Product](json)