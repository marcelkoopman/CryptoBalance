import CryptoBalance.httpActor
import HttpActor.{BtcBalance, BtcEUR, EthBalance, EthEUR}
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import spray.json._
import DefaultJsonProtocol._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success

/**
  * Created by marcel on 17-4-17.
  */
object CryptoBalance extends App {

  println("CryptoBalance")

  val system = ActorSystem()
  val httpActor = system.actorOf(HttpActor.props, "httpActor")

  implicit val timeout = Timeout(5 seconds)

  case class EthPriceResult(status: String, message: String, result: String)

  case class PriceResult(EUR: BigDecimal)

  implicit val ethPriceResult = jsonFormat3(EthPriceResult)
  implicit val priceResult = jsonFormat1(PriceResult)

  val etherPrice: Future[BigDecimal] = {
    (httpActor ? EthEUR).mapTo[String].map {
      result =>
        val json = result.parseJson
        val priceResult = json.convertTo[PriceResult]
        priceResult.EUR
    }
  }

  val ether: Future[BigDecimal] =
    (httpActor ? EthBalance("0xbd31fa88f89699ff2eb3d66b449de77e79bb2053")).mapTo[String].map {
      result =>
        val json = result.parseJson
        val priceResult = json.convertTo[EthPriceResult]
        val wei = priceResult.result.toDouble
        BigDecimal(wei / 1000000000000000000L)
    }

  val btcPrice: Future[BigDecimal] = {
    (httpActor ? BtcEUR).mapTo[String].map {
      result =>
        val json = result.parseJson
        val priceResult = json.convertTo[PriceResult]
        priceResult.EUR
    }
  }

  val btc: Future[BigDecimal] = (httpActor ? BtcBalance("1M693Ay4qWSS3o3mTbxhmBZ2h1dF9cdw48")).map {
    result =>
      val Coin = 100000000L
      val satoshi = result.asInstanceOf[String].toDouble
      BigDecimal(satoshi) / Coin
  }

  val result = for {
    ethPrice <- etherPrice
    ethBalance <- ether
    btcPrice <- btcPrice
    btcBalance <- btc
  } yield (ethPrice, ethBalance, btcPrice, btcBalance)

  result.map { r =>
    val euroBalanceEth = r._1 * r._2
    println("ETH "+r._2+" = "+euroBalanceEth+" EUR")
    val euroBalanceBtc = r._3 * r._4
    println("BTC "+r._3+" = "+euroBalanceBtc+" EUR")
    println("Total "+(euroBalanceEth + euroBalanceEth)+ " EUR")
  }.recover {
    case e:Exception => e.printStackTrace()
  }

  sys.addShutdownHook(system.terminate())

}
