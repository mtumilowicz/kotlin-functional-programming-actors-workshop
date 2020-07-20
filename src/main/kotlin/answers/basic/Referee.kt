package answers.basic

import answers.core.AbstractActor
import answers.core.Actor

class Referee : AbstractActor<Int>("Referee") {
    override fun onReceive(message: Int, sender: Actor<Int>) {
        println("Game ended after $message shots")
    }
}