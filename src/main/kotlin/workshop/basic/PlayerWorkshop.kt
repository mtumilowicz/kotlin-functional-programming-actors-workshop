package workshop.basic

import answers.actor.AbstractActor
import answers.actor.Actor

class PlayerWorkshop(
    id: String,
    private val referee: Actor<Int>
) : AbstractActor<Int>(id) {
    override fun handle(message: Int, sender: Actor<Int>) {
        // Ping/Pong (differs on the player name) - number of hit, hint: println, "$..."
        // if more than 10 hits - referee
        // else bounce back to sender with incremented count
    }
}