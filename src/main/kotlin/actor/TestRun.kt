package actor

import java.util.concurrent.Executors

class Ping(val sender: ActorRef<Pong>)

class Pong(val sender: ActorRef<Ping>)

class Player1(
    val self: ActorRef<Ping>,
    var counter: Int = 0
) {
     fun behaviour(msg: Ping): Behaviour<Ping> {
        return if (counter < 10) {
            println("ping!")
            msg.sender.tell(Pong(self))
            this.counter++
            Behaviour { behaviour(it) }
        } else {
            println("last ping!")
            Behaviour { behaviour(it) }
        }
    }
}

fun main() {

    fun player2(self: ActorRef<Pong>, msg: Pong): Behaviour<Pong> {
        println("pong!")
        msg.sender.tell(Ping(self))
        return Behaviour { m -> player2(self, m) }
    }

    val actorSystem = ActorSystem(Executors.newCachedThreadPool())
    val player1 = actorSystem.spawn { self: ActorRef<Ping> ->
        Behaviour { msg ->
            Player1(self).behaviour(msg)
        }
    }
    val player2 = actorSystem.spawn { self: ActorRef<Pong> ->
        Behaviour { msg ->
            player2(
                self,
                msg
            )
        }
    }
    player1.tell(Ping(player2))
    Thread.sleep(3_000)
    actorSystem.shutdown()

}