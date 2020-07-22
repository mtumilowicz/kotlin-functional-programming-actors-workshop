package workshop.basic

import workshop.actor.AbstractActorWorkshop
import workshop.actor.ActorWorkshop

class PlayerWorkshop(
    id: String,
    private val referee: ActorWorkshop<Int>
) : AbstractActorWorkshop<Int>(id) {
    // hint: override handle
    // Ping/Pong (differs on the player name) - number of hit, hint: println, "$..."
    // if more than 10 hits - referee
    // else bounce back to sender with incremented count
}