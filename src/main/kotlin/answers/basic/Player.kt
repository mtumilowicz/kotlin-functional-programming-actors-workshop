package answers.basic

import answers.actor.AbstractActor
import answers.actor.Actor

class Player(
    id: String,
    private val referee: Actor<Int>
) : AbstractActor<Int>(id) {
    override fun handle(message: Int, sender: Actor<Int>) {
        println("$id - $message")
        if (message >= 10) {
            referee.enqueue(message, sender)
        } else {
            sender.enqueue(message + 1, self())
        }
    }
}