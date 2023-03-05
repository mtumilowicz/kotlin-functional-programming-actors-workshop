package actor

import java.util.concurrent.Executors

class Ping(val sender: ActorRef<Pong>)

class Pong(val sender: ActorRef<Ping>)

class StatefulPonger(
    val self: ActorRef<Ping>,
    var counter: Int = 0
) {
     fun behaviour(msg: Ping): Behavior<Ping> {
        return if (counter < 10) {
            println("ping! ➡️")
            msg.sender.tell(Pong(self))
            this.counter++
            Behavior { behaviour(it) }
        } else {
            println("ping! ☠️")
            Behavior { behaviour(it) }
        }
    }
}

fun main() {

    fun pingerBehavior(self: ActorRef<Pong>, msg: Pong): Behavior<Pong> {
        println("pong! ⬅️")
        msg.sender.tell(Ping(self))
        return Behavior { m -> pingerBehavior(self, m) }
    }

    var actorSystem = ActorSystem(Executors.newCachedThreadPool())
    var ponger = actorSystem.spawn { self: ActorRef<Ping> ->
        Behavior { msg ->
            StatefulPonger(self).behaviour(msg)
        }
    }
    var pinger = actorSystem.spawn { self: ActorRef<Pong> ->
        Behavior { msg ->
            pingerBehavior(
                self,
                msg
            )
        }
    }
    ponger.tell(Ping(pinger))
    actorSystem.shutdown()

}