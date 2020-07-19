package advanced

import core.IntTaskInput
import core.Task
import core.TaskIndex

class ComputeFibonacciTask(
    index: TaskIndex,
    input: IntTaskInput,
    val output: FibonacciTaskOutput? = null
) : Task<IntTaskInput>(index, input) {
    fun run(): ComputeFibonacciTask = ComputeFibonacciTask(index, input, FibonacciTaskOutput(Fibonacci.count(input.raw)))
}