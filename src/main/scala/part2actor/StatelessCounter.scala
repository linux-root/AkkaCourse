package part2actor

import akka.actor.{Actor, ActorSystem, Props}

object StatelessCounter extends App{
  val akkaSystem = ActorSystem("vl-system")
  val counter = akkaSystem.actorOf(Props[Counter], "stateless-counter")

  counter ! Increment
  counter ! Decrement
  counter ! Decrement
  counter ! Increment
  counter ! Increment
  counter ! Print // result : 1

  class Counter extends Actor {
    def receive: Receive = {
      case Increment => context become setValue(1)
      case Decrement => context become setValue(-1)
      case Print => println(0)
    }

    def setValue(value: Int): Receive = {
      case Print => println(value)
      case Decrement => context become setValue(value - 1)
      case Increment => context become setValue(value + 1)
    }
  }

  case object Increment
  case object Decrement
  case object Print
}



