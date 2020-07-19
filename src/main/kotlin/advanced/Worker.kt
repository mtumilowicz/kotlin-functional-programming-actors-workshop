package advanced

import core.AbstractActor
import core.Actor

class Worker(id: String) : AbstractActor<Pair<Int, Int>>(id) {

    override fun onReceive(
        message: Pair<Int, Int>,
        sender: Actor<Pair<Int, Int>>
    ) {
        sender.receive(Pair(fibonacci(message.first), message.second))
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

