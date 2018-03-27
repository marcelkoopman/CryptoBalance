
import BalanceActor.RetrieveBalance
import SupervisingActor.LogStats
import akka.actor.ActorSystem

/**
  * Created by marcel on 17-4-17.
  */
object CryptoBalance extends App {

  val system = ActorSystem()
  val balanceActor = system.actorOf(BalanceActor.props, "balanceActor")
  val supervisingActor = system.actorOf(SupervisingActor.props, "supervisingActor")

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  system.scheduler.schedule(0 seconds, 30 seconds, balanceActor, RetrieveBalance())
  system.scheduler.schedule(0 seconds, 10 seconds, supervisingActor, LogStats())

  sys.addShutdownHook(system.terminate())
}
