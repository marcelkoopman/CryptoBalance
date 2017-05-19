import HttpActor.{BtcEUR, EthEUR}
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
  def props:Props = Props[HttpActor]
}

class HttpActor extends Actor with ActorLogging{

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  def receive = {
    case EthEUR => getPrice("https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=EUR")
    case BtcEUR => getPrice("https://min-api.cryptocompare.com/data/price?fsym=BTC&tsyms=EUR")
  }

  private def getPrice(url:String) = {
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
