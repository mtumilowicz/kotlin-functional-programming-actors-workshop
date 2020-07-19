package advanced

import core.IntTaskInput
import core.Task
import core.TaskIndex

class ComputeFibonacciTask(
    index: TaskIndex,
    input: IntTaskInput,
    output: FibonacciTaskOutput? = null
) : Task<IntTaskInput, FibonacciTaskOutput>(index, input, output) {
    fun run(): ComputeFibonacciTask =
        ComputeFibonacciTask(index, input, FibonacciTaskOutput(Fibonacci.count(input.raw)))
}