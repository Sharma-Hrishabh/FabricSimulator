//#full-example
package com.example

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.Props
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.LoggerOps
import com.example.InitMain.bootStrap
import com.example.Endorser.submitTransaction


object Endorser {
  // final case class Endorsed(whom:String, replyTo: ActorRef[])
  final case class submitTransaction(whom:String)
  def apply(): Behavior[submitTransaction] = Behaviors.receive { (context, message) =>
    println(this)
    context.log.info("Endorsed!", message.whom)
    // message.replyTo ! Endorsed(message.whom, context.self)
    Behaviors.same
  } 
}


object Organisation {
  final case class spawnPeers(whom:String,name:String)
  def apply(): Behavior[spawnPeers] = Behaviors.receive { (context, message) =>
    context.log.info("Creating peers in an org!", message.whom)
    // message.replyTo ! Endorsed(message.whom, context.self)
    val peer1 = context.spawn(Endorser(), message.name+":endorser1")
    val peer2 = context.spawn(Endorser(), message.name+":endorser2")

    peer1 ! Endorser.submitTransaction(message.whom)
    peer2 ! Endorser.submitTransaction(message.whom)

    Behaviors.same
  } 

}

object Client {

//  final case class createTransaction(whom:String,replyTo: ActorRef[submitTransaction])
 final case class createTransaction(whom:String)

 def apply(): Behavior[createTransaction] =  Behaviors.receive { (context, message) =>
    context.log.info("Client: Submitting {}!", message.whom)

    //create organisations
    val org1 = context.spawn(Organisation(), "org1")
    val org2 = context.spawn(Organisation(), "org2")
    val org3 = context.spawn(Organisation(), "org3")

    org1 ! Organisation.spawnPeers(message.whom,"org1")
    org2 ! Organisation.spawnPeers(message.whom,"org2")
    org3 ! Organisation.spawnPeers(message.whom,"org3")

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
        // val replyTo = context.spawn(Endorser(), "endorser")



        // val replyTo = context.spawn(Endorser(), "endorser")
        // client ! Client.createTransaction(message.name,replyTo)
        client ! Client.createTransaction(message.name)

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
