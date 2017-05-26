import HttpActor.{BtcBalance, BtcEUR, EthBalance, EthEUR}
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import spray.json._
import DefaultJsonProtocol._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
/**
  * Created by marcel on 17-4-17.
  */
object CryptoBalance extends App {

  println("CryptoBalance")

  val system = ActorSystem()
  val httpActor = system.actorOf(HttpActor.props, "httpActor")

  implicit val timeout = Timeout(5 seconds)

  case class EthPriceResult(status:String, message:String, result:String)
  implicit val ethPriceResult = jsonFormat3(EthPriceResult)

  (httpActor ? EthEUR).map { json => println("ETH = "+json) }
  (httpActor ? BtcEUR).map { json => println("BTC = "+json) }
  (httpActor ? EthBalance("0xbd31fa88f89699ff2eb3d66b449de77e79bb2053")).mapTo[String].map {
    result =>
      val json = result.parseJson
      val priceResult = json.convertTo[EthPriceResult]
      val wei = priceResult.result.toDouble
      val eth = BigDecimal(wei / 1000000000000000000L)
      println("eth balance = "+eth+" ETH")
  }
  (httpActor ? BtcBalance("1M693Ay4qWSS3o3mTbxhmBZ2h1dF9cdw48")).map {
    result =>
      val Coin = 100000000L
      val satoshi = result.asInstanceOf[String].toDouble
      val btc = BigDecimal(satoshi) / Coin
      println("btc balance = "+btc+ " BTC")
  }

  sys.addShutdownHook( system.terminate() )

}
