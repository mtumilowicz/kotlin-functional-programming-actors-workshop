package answers.newactor

import java.util.concurrent.Executors

class Ping(val sender: TypedActor.Address<Pong>)

class Pong(val sender: TypedActor.Address<Ping>)

class StatefulPonger(
    val self: TypedActor.Address<Ping>,
    var counter: Int = 0
) : TypedActor.Behavior<Ping> {
    override fun invoke(msg: Ping): TypedActor.Effect<Ping> {
        return if (counter < 10) {
            println("ping! ➡️");
            msg.sender.tell(Pong(self))
            this.counter++
            TypedActor.Stay()
        } else {
            println("ping! ☠️")
            TypedActor.Stay()
        }
    }
}

fun main() {

    fun pingerBehavior(self: TypedActor.Address<Pong>, msg: Pong): TypedActor.Effect<Pong> {
        println("pong! ⬅️")
        msg.sender.tell(Ping(self))
        return TypedActor.Stay()
    }

    var actorSystem = TypedActor.System(Executors.newCachedThreadPool())
    var ponger = actorSystem.actorOf { StatefulPonger(it) }
    var pinger = actorSystem.actorOf { self: TypedActor.Address<Pong> ->
        TypedActor.Behavior { msg ->
            pingerBehavior(
                self,
                msg
            )
        }
    }
    ponger.tell(Ping(pinger))

}