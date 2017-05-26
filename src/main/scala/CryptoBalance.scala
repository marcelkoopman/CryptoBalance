import HttpActor.{BtcBalance, BtcEUR, EthBalance, EthEUR}
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Created by marcel on 17-4-17.
  */
object CryptoBalance extends App {

  println("CryptoBalance")

  val system = ActorSystem()
  val httpActor = system.actorOf(HttpActor.props, "httpActor")

  implicit val timeout = Timeout(5 seconds)
  val etherPrice: Future[BigDecimal] = {
    (httpActor ? EthEUR).mapTo[String].map {
      result =>
        val json = result.parseJson
        val priceResult = json.convertTo[PriceResult]
        priceResult.EUR
    }
  }
  val btcPrice: Future[BigDecimal] = {
    (httpActor ? BtcEUR).mapTo[String].map {
      result =>
        val json = result.parseJson
        val priceResult = json.convertTo[PriceResult]
        priceResult.EUR
    }
  }

  implicit val ethPriceResult = jsonFormat3(EthPriceResult)
  implicit val priceResult = jsonFormat1(PriceResult)
  val result = for {
    ethPrice <- etherPrice
    ethBalance <- ether(ethPrice)
    btcPrice <- btcPrice
    btcBalance <- btc(btcPrice)
  } yield (ethBalance, btcBalance)

  def ether(ethPrice: BigDecimal): Future[BigDecimal] =
    (httpActor ? EthBalance("0xbd31fa88f89699ff2eb3d66b449de77e79bb2053")).mapTo[String].map {
      result =>
        val json = result.parseJson
        val priceResult = json.convertTo[EthPriceResult]
        val wei = priceResult.result.toDouble
        val eth = BigDecimal(wei / 1000000000000000000L)
        println("ETH balance = " + eth)
        eth * ethPrice
    }

  def btc(btcPrice: BigDecimal): Future[BigDecimal] = (httpActor ? BtcBalance("1M693Ay4qWSS3o3mTbxhmBZ2h1dF9cdw48")).map {
    result =>
      val Coin = 100000000L
      val satoshi = result.asInstanceOf[String].toDouble
      val btc = BigDecimal(satoshi) / Coin
      println("BTC balance = " + btc)
      btc * btcPrice
  }

  case class EthPriceResult(status: String, message: String, result: String)

  case class PriceResult(EUR: BigDecimal)

  result.map { r =>
    val euroBalanceEth = r._1
    println("ETH = €" + euroBalanceEth)
    val euroBalanceBtc = r._2
    println("BTC = €" + euroBalanceBtc)
    println("Total = €" + (euroBalanceEth + euroBalanceEth))
  }.recover {
    case e:Exception => e.printStackTrace()
  }

  sys.addShutdownHook(system.terminate())

}
