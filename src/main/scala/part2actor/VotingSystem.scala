package part2actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object VotingSystem extends App{

  val actorSystem = ActorSystem("voting-system")
  val ag = actorSystem.actorOf(Props[VoteAggregator], "ag")
  val bob = actorSystem.actorOf(Props[Citizen], "bob")
  val alice = actorSystem.actorOf(Props[Citizen], "alice")
  val tom = actorSystem.actorOf(Props[Citizen], "tom")
  val ted = actorSystem.actorOf(Props[Citizen], "ted")
  val slugVoter = actorSystem.actorOf(Props[Citizen], "slugVoter")
  val slugVoter1 = actorSystem.actorOf(Props[Citizen], "slugVoter1")
  val slugVoter2 = actorSystem.actorOf(Props[Citizen], "slugVoter2")

  // send vote
  bob ! Vote("Hung")
  alice ! Vote("Nam")
  tom ! Vote("Hung")
  ted ! Vote ("Tung")

  ag ! AggregateVotes(Set(bob, alice, tom, ted, slugVoter, slugVoter1, slugVoter2))

  Thread.sleep(3000) // waiting time for SlugVoters

  slugVoter ! Vote("Redpicasso")
  slugVoter1 ! Vote("Redpicasso")
  slugVoter2 ! Vote("Redpicasso")



  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusResponse(candidate: Option[String])
  case class AggregateVotes(citizens: Set[ActorRef])


  class VoteAggregator extends Actor {
    override def receive: Receive = {
      case AggregateVotes(voters) => {
        context become awaitingVoteResponse(voters, Map.empty)
        voters.foreach(_ ! VoteStatusRequest)
      }
    }

    def awaitingVoteResponse(awaitingVoters : Set[ActorRef], result : Map[String, Int]) : Receive = {
      case VoteStatusResponse(Some(candidate)) => {
        val candidateVote = result.getOrElse(candidate, 0)

        val newAwaitingVoters = awaitingVoters - sender()
        val newResult = result + (candidate -> (candidateVote + 1))

        if (newAwaitingVoters.isEmpty) {
          newResult.foreach(x => println(s"${x._1}: ${x._2}"))
        } else {
          context become awaitingVoteResponse(newAwaitingVoters, newResult)
        }
      }

      case VoteStatusResponse(None) =>
        sender() ! VoteStatusRequest // send request until get Some(candidate) result
        println(s"waiting for ${sender().path} to vote...")
    }
  }

  class Citizen extends Actor {
    def receive: Receive = {
      case Vote(candidate: String) => context become voted(candidate)
      case VoteStatusRequest => sender() ! VoteStatusResponse(None)
    }

    def voted(candidate: String) : Receive = {
      case VoteStatusRequest => sender() ! VoteStatusResponse(Some(candidate))
    }
  }
}
