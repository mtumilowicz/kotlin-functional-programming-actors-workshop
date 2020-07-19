package advanced

import core.AbstractActor
import core.Actor
import core.IntTaskInput

class Worker(id: String) : AbstractActor<ComputeFibonacciTask>(id) {

    override fun onReceive(
        message: ComputeFibonacciTask,
        sender: Actor<ComputeFibonacciTask>
    ) {
        val task = ComputeFibonacciTask(message.index, IntTaskInput(Fibonacci.count(message.input.raw)))
        sender.receive(task, self())
    }
}

