package answers.advanced

import answers.core.AbstractActor
import answers.core.Actor

class Worker(id: String) : AbstractActor<ComputeFibonacciTask>(id) {

    override fun onReceive(message: ComputeFibonacciTask, sender: Actor<ComputeFibonacciTask>) {
        val task = message.run()
        sender.receive(task, self())
    }
}

