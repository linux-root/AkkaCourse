package part2actor

import akka.actor.ActorSystem

object ActorIntro extends App{
  val akkaSystem = ActorSystem("vl")
  println(akkaSystem.name)
}
