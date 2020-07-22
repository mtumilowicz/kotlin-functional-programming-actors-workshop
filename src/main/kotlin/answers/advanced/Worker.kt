package answers.advanced

import answers.actor.AbstractActor
import answers.actor.Actor
import common.fibonacci.ComputeFibonacciTask

class Worker(id: String) : AbstractActor<ComputeFibonacciTask>(id) {

    override fun handle(message: ComputeFibonacciTask, sender: Actor<ComputeFibonacciTask>) {
        val task = message.run()
        sender.enqueue(task, self())
    }
}

