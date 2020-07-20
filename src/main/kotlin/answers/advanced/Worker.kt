package answers.advanced

import answers.actor.AbstractActor
import answers.actor.Actor

class Worker(id: String) : AbstractActor<ComputeFibonacciTask>(id) {

    override fun onReceive(message: ComputeFibonacciTask, sender: Actor<ComputeFibonacciTask>) {
        val task = message.run()
        sender.receive(task, self())
    }
}

