package common.fibonacci

object Fibonacci {
    fun count(n: Int): Int {
        tailrec fun fibonacci(prev: Int, next: Int, counter: Int = n): Int =
            when (counter) {
                0 -> 1
                1 -> prev + next
                else -> fibonacci(next, prev + next, counter - 1)
            }
        return fibonacci(0, 1)
    }
}