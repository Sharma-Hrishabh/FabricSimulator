//#full-example
package com.example

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.example.InitMain.bootStrap
import com.example.Endorser.submitTransaction


object Endorser {
  // final case class Endorsed(whom:String, replyTo: ActorRef[])
  final case class submitTransaction(whom:String)
  def apply(): Behavior[submitTransaction] = Behaviors.receive { (context, message) =>
    context.log.info("Endorsed!", message.whom)
    // message.replyTo ! Endorsed(message.whom, context.self)
    Behaviors.same
  }
}


object Client {

 final case class createTransaction(whom:String,replyTo: ActorRef[submitTransaction])
 def apply(): Behavior[createTransaction] =  Behaviors.receive { (context, message) =>
    context.log.info("Client: Submitting {}!", message.whom)
    message.replyTo ! Endorser.submitTransaction(message.whom)
    Behaviors.same
  }
}

object InitMain {

final case class bootStrap(name: String)

  def apply(): Behavior[bootStrap] =
    Behaviors.setup { context =>

      val client = context.spawn(Client(),"client")
      //#create-actors

      Behaviors.receiveMessage { message =>
        val replyTo = context.spawn(Endorser(), "endorser")        
        client ! Client.createTransaction(message.name,replyTo)

        Behaviors.same
      }
    }
}

//#main-class
object FabricSimulator extends App {
  //#actor-system
  val initMain: ActorSystem[InitMain.bootStrap] = ActorSystem(InitMain(), "FabricSimulator")
  initMain ! bootStrap("TX:A")
}
