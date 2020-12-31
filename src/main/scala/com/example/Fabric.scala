//#full-example
package com.example


import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.example.InitMain.TxnProposal


object Endorser {

  // final case class Endorsed(whom:String, replyTo: ActorRef[])
  final case class submitTrasaction(whom:String)
  def apply(): Behavior[submitTrasaction] = Behaviors.receive { (context, message) =>
    context.log.info("Hello {}!", message.whom)
    //#greeter-send-messages
    // message.replyTo ! Endorsed(message.whom, context.self)
    //#greeter-send-messages
    Behaviors.same
  }
}


object InitMain {

final case class TxnProposal(name: String)

  def apply(): Behavior[TxnProposal] =
    Behaviors.setup { context =>

      val endorser = context.spawn(Endorser(),"endorser")
      //#create-actors

      Behaviors.receiveMessage { message =>

        endorser ! Endorser.submitTrasaction(message.name)
        // greeter ! Greeter.Greet(message.name, replyTo)
        Behaviors.same
      }
    }
}


//#main-class
object FabricSimulator extends App {
  //#actor-system
  val initMain: ActorSystem[InitMain.TxnProposal] = ActorSystem(InitMain(), "FabricSimulator")
  //#actor-system

  //#main-send-messages
  initMain ! TxnProposal("TX:A")
  //#main-send-messages
}
//#main-class
