package basic

import core.AbstractActor
import core.Actor

class Player(
    id: String,
    private val sound: String,
    private val referee: Actor<Int>
) : AbstractActor<Int>(id) {
    override fun onReceive(message: Int, sender: Actor<Int>) {
        println("$sound - $message")
        if (message >= 10) {
            referee.receive(message, sender)
        } else {
            sender.receive(message + 1)
        }
    }
}