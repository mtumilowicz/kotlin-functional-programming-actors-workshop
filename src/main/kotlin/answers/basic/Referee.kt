package answers.basic

import answers.core.actor.AbstractActor
import answers.core.actor.Actor

class Referee : AbstractActor<Int>("Referee") {
    override fun onReceive(message: Int, sender: Actor<Int>) {
        println("Game ended after $message shots")
    }
}