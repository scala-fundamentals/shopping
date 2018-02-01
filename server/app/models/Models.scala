package models

case class Product(name: String, code : String, description : String, price: Double)

case class Cart(user:String, productCode: String, quantity: Int)

case class User(sessionID: String)
