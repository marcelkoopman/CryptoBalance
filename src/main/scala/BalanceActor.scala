import BalanceActor._
import HttpActor.{BtcBalance, BtcEUR, EthBalance, EthEUR}
import akka.actor.{Actor, ActorLogging, Props}

/**
  * Created by marcel on 26-05-17.
  */

object BalanceActor {
  def props: Props = Props[BalanceActor]

  case class RetrieveBalance()

  case class ReceiveEthEurPrice(price: BigDecimal)

  case class ReceiveBtcEurPrice(price: BigDecimal)

  case class ReceiveEthBalance(price: BigDecimal)

  case class ReceiveBtcBalance(price: BigDecimal)

}

class BalanceActor extends Actor with ActorLogging {

  private val httpActor = context.actorOf(HttpActor.props, "httpActor")

  def receive = {
    case RetrieveBalance() =>
      log.info("Retrieving balances...")
      httpActor ! EthEUR
      httpActor ! BtcEUR

    case ReceiveEthEurPrice(price: BigDecimal) =>
      log.info("Euro price ETH = " + price)
      httpActor ! EthBalance("0xbd31fa88f89699ff2eb3d66b449de77e79bb2053", price)

    case ReceiveBtcEurPrice(price: BigDecimal) =>
      log.info("Euro price BTC = " + price)
      httpActor ! BtcBalance("1M693Ay4qWSS3o3mTbxhmBZ2h1dF9cdw48", price)

    case ReceiveEthBalance(price: BigDecimal) =>
      log.info("Euro value of ETH = " + price)

    case ReceiveBtcBalance(price: BigDecimal) =>
      log.info("Euro value of BTC = " + price)
  }

}


