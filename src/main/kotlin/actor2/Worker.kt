package actor2

import actor.AbstractActor
import actor.Actor

class Worker(id: String) : AbstractActor<Int>(id) {
    override fun onReceive(message: Int, sender: Result<Actor<Int>>) {
        sender.onSuccess { a: Actor<Int> ->
            a.tell(slowFibonacci(message), self())
        }
    }
    private fun slowFibonacci(number: Int): Int {
        // An inefficient algorithm is used on purpose to create long-lasting tasks.
        return when (number) {
            0 -> 1
            1 -> 1
            else -> slowFibonacci(number - 1) + slowFibonacci(number - 2)
        }
    }
}