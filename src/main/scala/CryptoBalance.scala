import HttpActor.{BtcEUR, EthBalance, EthEUR}
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
/**
  * Created by marcel on 17-4-17.
  */
object CryptoBalance extends App{

  println("CryptoBalance")

  val system = ActorSystem()
  val httpActor = system.actorOf(HttpActor.props, "httpActor")

  implicit val timeout = Timeout(5 seconds)
  (httpActor ? EthEUR).map { json => println("eth = "+json) }
  (httpActor ? BtcEUR).map { json => println("btc = "+json) }


  (httpActor ? EthBalance("0xbd31fa88f89699ff2eb3d66b449de77e79bb2053")).map { json => println("eth balance = "+json) }


  sys.addShutdownHook( system.terminate() )

}
