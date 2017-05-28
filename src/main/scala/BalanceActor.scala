import java.time.LocalDateTime

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

  private var balanceMap = scala.collection.mutable.Map[String, BigDecimal]()
  private var priceHistory = scala.collection.mutable.Map[LocalDateTime, BigDecimal]()

  def receive = {
    case RetrieveBalance() =>
      log.info("----------------------")
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
      balanceMap += ("ETH" -> price)
      printTotalBalance

    case ReceiveBtcBalance(price: BigDecimal) =>
      balanceMap += ("BTC" -> price)
      printTotalBalance
  }

  private def printTotalBalance(): Unit = {
    if (balanceMap.size == 2) {
      val newPrice = balanceMap.values.sum
      if (priceHistory.isEmpty) {
        log.info("First price {}", newPrice)
      } else {
        val high = priceHistory.values.max
        val diff = high - newPrice
        if (diff < 0)
          log.info("UP! {} by {}", newPrice, diff)
        else if (diff > 0)
          log.info("DOWN {} by {}", newPrice, diff)
        else
          log.info("SAME {}", newPrice)
      }
      priceHistory += (LocalDateTime.now() -> newPrice)
      balanceMap.clear()
    }
  }

}


