package advanced

import core.AbstractActor
import core.Actor
import core.TaskIndex
import core.TaskInput

class Worker(id: String) : AbstractActor<ComputeFibonacciTask>(id) {

    override fun onReceive(
        message: ComputeFibonacciTask,
        sender: Actor<ComputeFibonacciTask>
    ) {
        sender.receive(ComputeFibonacciTask(message.index, TaskInput(fibonacci(message.input.raw))), self())
    }

    private fun fibonacci(n: Int): Int {
        tailrec fun fibonacci(prev: Int, next: Int, counter: Int = n): Int =
            when (counter) {
                0 -> 1
                1 -> prev + next
                else -> fibonacci(next, prev + next, counter - 1)
            }
        return fibonacci(0, 1)
    }
}

