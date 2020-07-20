package answers.basic

import answers.actor.AbstractActor
import answers.actor.Actor

class Referee : AbstractActor<Int>("Referee") {
    override fun handle(message: Int, sender: Actor<Int>) {
        println("Game ended after $message shots")
    }
}