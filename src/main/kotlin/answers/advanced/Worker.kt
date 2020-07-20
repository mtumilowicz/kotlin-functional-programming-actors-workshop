package answers.advanced

import answers.core.actor.AbstractActor
import answers.core.actor.Actor

class Worker(id: String) : AbstractActor<ComputeFibonacciTask>(id) {

    override fun onReceive(message: ComputeFibonacciTask, sender: Actor<ComputeFibonacciTask>) {
        val task = message.run()
        sender.receive(task, self())
    }
}

