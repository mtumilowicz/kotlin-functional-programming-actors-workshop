package actor

import java.util.concurrent.Executors

class Ping(val sender: ActorRef<Pong>)

class Pong(val sender: ActorRef<Ping>)

class StatefulPonger(
    val self: ActorRef<Ping>,
    var counter: Int = 0
) : Behavior<Ping> {
    override fun invoke(msg: Ping): Behavior<Ping> {
        return if (counter < 10) {
            println("ping! ➡️")
            msg.sender.tell(Pong(self))
            this.counter++
            this
        } else {
            println("ping! ☠️")
            this
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
    var ponger = actorSystem.spawn { StatefulPonger(it) }
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