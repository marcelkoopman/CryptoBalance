
import BalanceActor.RetrieveBalance
import akka.actor.ActorSystem

/**
  * Created by marcel on 17-4-17.
  */
object CryptoBalance extends App {

  val system = ActorSystem()
  val balanceActor = system.actorOf(BalanceActor.props, "balanceActor")

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  system.scheduler.schedule(1 seconds, 5 seconds, balanceActor, RetrieveBalance())

  sys.addShutdownHook(system.terminate())

}
