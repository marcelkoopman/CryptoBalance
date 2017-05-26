import HttpActor.{BtcEUR, EthBalance, EthEUR}
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by marcel on 17-4-17.
  */

object HttpActor {
  case class EthEUR()
  case class BtcEUR()
  case class EthBalance(address:String)
  def props:Props = Props[HttpActor]
}

class HttpActor extends Actor with ActorLogging{

  private val apiKey = "GX4JRHSB4XT6FGR14AMWB9229NXNZFP1WA"

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  def receive = {
    case EthEUR => getJsonResult("https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=EUR")
    case BtcEUR => getJsonResult("https://min-api.cryptocompare.com/data/price?fsym=BTC&tsyms=EUR")
    case EthBalance(address) => getJsonResult(buildUri(address))
  }

  private def buildUri(address:String):String = {
    "https://api.etherscan.io/api?module=account&action=balance&address="+address+"&tag=latest&apikey="+apiKey
  }

  private def getJsonResult(url:String) = {
    val theSender = sender()
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url))

    responseFuture.onComplete {
      case Success(response) =>
        Unmarshal(response.entity).to[String].map {
          text => theSender ! text
        }
      case Failure(f) => theSender ! f.getLocalizedMessage
    }
  }

}
