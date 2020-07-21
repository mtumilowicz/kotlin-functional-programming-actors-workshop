package workshop.basic

import answers.actor.AbstractActor
import answers.actor.Actor

class RefereeWorkshop : AbstractActor<Int>("Referee") {
    override fun handle(message: Int, sender: Actor<Int>) {
        // print that game ended, hint: Game ended after xxx shots
    }
}