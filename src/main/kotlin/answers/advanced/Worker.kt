package answers.advanced

import answers.actor.AbstractActor
import answers.actor.Actor

class Worker(id: String) : AbstractActor<ComputeFibonacciTask>(id) {

    override fun handle(message: ComputeFibonacciTask, sender: Actor<ComputeFibonacciTask>) {
        val task = message.run()
        sender.enqueue(task, self())
    }
}

