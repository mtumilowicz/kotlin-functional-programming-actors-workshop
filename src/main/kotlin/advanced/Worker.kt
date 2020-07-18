package advanced

import core.AbstractActor
import core.Actor

class Worker(id: String) : AbstractActor<Pair<Int, Int>>(id) {

    override fun onReceive(
        message: Pair<Int, Int>,
        sender: Actor<Pair<Int, Int>>
    ) {
        sender.tell(Pair(fibonacci(message.first), message.second), self())
    }

    private fun fibonacci(number: Int): Int {
        tailrec fun fibonacci(acc1: Int, acc2: Int, x: Int): Int = when (x) {
            0 -> 1
            1 -> acc1 + acc2
            else -> fibonacci(acc2, acc1 + acc2, x - 1)
        }
        return fibonacci(0, 1, number)
    }
}

