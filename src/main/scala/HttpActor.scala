import BalanceActor._
import HttpActor.{BtcBalance, BtcEUR, EthBalance, EthEUR, EthPriceResult, PriceResult}
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol._
import spray.json._

/**
  * Created by marcel on 17-4-17.
  */

object HttpActor {

  def props: Props = Props[HttpActor]

  implicit val priceResult = jsonFormat1(PriceResult)

  case class PriceResult(EUR: BigDecimal)

  implicit val ethPriceResult = jsonFormat3(EthPriceResult)

  case class EthPriceResult(status: String, message: String, result: BigDecimal)

  case class EthEUR()

  case class BtcEUR()

  case class EthBalance(address: String, price: BigDecimal)

  case class BtcBalance(address: String, price: BigDecimal)

}

class HttpActor extends Actor with ActorLogging{


  import scala.concurrent.ExecutionContext.Implicits.global

  private val apiKey = "GX4JRHSB4XT6FGR14AMWB9229NXNZFP1WA"

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  def receive = {
    case EthEUR =>
      val theSender = sender()
      Http().singleRequest(HttpRequest(uri = "https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=EUR")).map {
        response =>
          Unmarshal(response.entity).to[String].map {
            text =>
              val json = text.parseJson
              val priceResult = json.convertTo[PriceResult]
              theSender ! ReceiveEthEurPrice(priceResult.EUR)
          }
      }.recover {
        case ex: Exception => log.error("EthEUR", ex)
      }
    case BtcEUR =>
      val theSender = sender()
      Http().singleRequest(HttpRequest(uri = "https://min-api.cryptocompare.com/data/price?fsym=BTC&tsyms=EUR")).map {
        response =>
          Unmarshal(response.entity).to[String].map {
            text =>
              val json = text.parseJson
              val priceResult = json.convertTo[PriceResult]
              theSender ! ReceiveBtcEurPrice(priceResult.EUR)
          }
      }.recover {
        case ex: Exception => log.error("BtcEUR", ex)
      }
    case EthBalance(address, price) =>
      val theSender = sender()
      Http().singleRequest(HttpRequest(uri = buildEthUri(address))).map {
        response =>
          Unmarshal(response.entity).to[String].map {
            text =>
              val json = text.parseJson
              val priceResult = json.convertTo[EthPriceResult]
              val wei = priceResult.result.toDouble
              val eth = BigDecimal(wei / 1000000000000000000L)
              theSender ! ReceiveEthBalance(eth * price)
          }
      }.recover {
        case ex: Exception => log.error("EthBalance", ex)
      }
    case BtcBalance(address, price) =>
      val theSender = sender()
      Http().singleRequest(HttpRequest(uri = buildBtcUri(address))).map {
        response =>
          Unmarshal(response.entity).to[String].map {
            text =>
              val satoshi = text.toDouble
              val btc = BigDecimal(satoshi / 100000000L)
              theSender ! ReceiveBtcBalance(btc * price)
          }
      }.recover {
        case ex: Exception => log.error("BtcBalance", ex)
      }
  }
  private def buildEthUri(address:String):String = {
    "https://api.etherscan.io/api?module=account&action=balance&address="+address+"&tag=latest&apikey="+apiKey
  }

  private def buildBtcUri(address: String):String = {
    "https://blockchain.info/nl/q/addressbalance/"+address
  }
}


