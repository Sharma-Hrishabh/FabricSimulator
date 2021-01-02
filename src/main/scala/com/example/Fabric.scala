//#full-example
package com.example


import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.example.InitMain.TxnProposal


object Endorser {

  // final case class Endorsed(whom:String, replyTo: ActorRef[])
  final case class submitTransaction(whom:String)
  def apply(): Behavior[submitTransaction] = Behaviors.receive { (context, message) =>
    context.log.info("Endorser: Hello {}!", message.whom)
    // message.replyTo ! Endorsed(message.whom, context.self)
    Behaviors.same
  }
}


object Client {

 final case class createTransaction(whom:String)
 def apply(): Behavior[createTransaction] =  Behaviors.receive { (context, message) =>
    context.log.info("Client: Hello {}!", message.whom)
    val replyTo = context.spawn(Endorser(), "endorser")
    replyTo ! Endorser.submitTransaction(message.whom)
    Behaviors.same
  }
}

object InitMain {

final case class TxnProposal(name: String)

  def apply(): Behavior[TxnProposal] =
    Behaviors.setup { context =>

      val client = context.spawn(Client(),"client")
      //#create-actors

      Behaviors.receiveMessage { message =>
        // val replyTo = context.spawn(Endorser(), "endorser")
        client ! Client.createTransaction(message.name)
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
