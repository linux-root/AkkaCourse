package part2actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

/**
 * word counter with child actors
 */
object WordCounter extends App{
  import Master._
  import Requester._

  val system = ActorSystem("word-counter-system")

  val requester = system.actorOf(Props[Requester], "requester")
  val master = system.actorOf(Props[Master], "master")
  master ! Initialize(3)

  requester ! InitializeMaster(master)

  Thread.sleep(500)

  requester ! "Kaycee oc hahah hahaha"
  requester ! "Kaycee oc"
  requester ! "Kaycee"
  requester ! "This is a six words sentence"


  object Master {
    case class Initialize(nWorker: Int)
    case class Task(text: String)
    case class Result(fromWorker: String, numberOfWords: Int)
  }

  class Master extends Actor {
    import Master._
    override def receive: Receive = {
      case Initialize(nWorker) =>
        for (i <- 0 until nWorker) {
          context.actorOf(Props[Worker], s"worker$i")
        }
        context become waitingTask(0, nWorker)
      case Result(from, numberOfWords) => println("this is impossible !")
    }

    def waitingTask(workerCounter: Int, nWorker: Int): Receive = {
      case (text : String) =>
        val workerSelection = context.actorSelection(s"/user/master/worker$workerCounter")
        workerSelection forward Task(text)
        val nextWorker = (workerCounter + 1) % nWorker
        context become waitingTask(nextWorker, nWorker)
    }
  }


  class Worker extends Actor {
    import Master.Result
    override def receive: Receive = {
      case Task(text) =>
        println(s"worker ${self.path} is processing message from ${sender().path}")
        sender() ! Result(getWorkerId(self.path.toString), countWords(text))

    }

    private def countWords(text: String) = text.split(" ").length

    private def getWorkerId(path: String) = {
      val parseWorkerIdRegex = ".*(worker\\d+)".r

      path match {
        case parseWorkerIdRegex(worker) => worker
       // case s"$something/worker$counter" => counter // alternative matching pattern!
        case _ => "error when parsing work id from path"
      }
    }
  }

  object Requester {
    case class InitializeMaster(master: ActorRef)
  }

  class Requester extends Actor {
    import Master._
    import Requester._
    override def receive : Receive = {
      case InitializeMaster(master: ActorRef) => context become masterInitialized(master)
      case (text : String) => println("[invalid message] please init master first")
    }

    def masterInitialized(master : ActorRef) : Receive = {
      case Result(from, numberOfWords) => println(s"[${from}]-$numberOfWords")
      case (text : String) => master ! text
    }
  }


}
