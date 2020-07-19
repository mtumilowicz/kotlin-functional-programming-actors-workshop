package advanced

import core.AbstractActor
import core.Actor

class Worker(id: String) : AbstractActor<ComputeFibonacciTask>(id) {

    override fun onReceive(
        message: ComputeFibonacciTask,
        sender: Actor<ComputeFibonacciTask>
    ) {
        val task = message.run()
        sender.receive(task, self())
    }
}

