import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import SupervisingActor._

object SupervisingActor {
  def props: Props = Props[SupervisingActor]

  case class LogStats()

}

class SupervisingActor extends Actor with ActorLogging {

  def receive = {
    case LogStats() => {
      import scala.collection.JavaConversions.mapAsScalaMap
      val free = org.apache.commons.io.FileUtils.byteCountToDisplaySize(Runtime.getRuntime.freeMemory)
      val total = org.apache.commons.io.FileUtils.byteCountToDisplaySize(Runtime.getRuntime.totalMemory)
      val usage = org.apache.commons.io.FileUtils.byteCountToDisplaySize(Runtime.getRuntime.totalMemory - Runtime.getRuntime.freeMemory)
      val perc = ((Runtime.getRuntime.totalMemory - Runtime.getRuntime.freeMemory) * 100) / (Runtime.getRuntime.totalMemory)
      if (perc > 65) {
        log.error("Heap space warning!!!")
        System.gc
      }
      log.info("Mem. utilization {}% ({} free, {} total)", perc, free, total)
      //for ((k,v) <- System.getenv) log.info("Environment var {}={}", k, v)
      //for ((k,v) <- System.getProperties) log.info("Property {}={}", k, v)
    }
  }
}