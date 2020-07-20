package answers.advanced

import answers.core.IntTaskInput
import answers.core.Task
import answers.core.TaskIndex

class ComputeFibonacciTask(
    index: TaskIndex,
    input: IntTaskInput,
    output: FibonacciTaskOutput? = null
) : Task<IntTaskInput, FibonacciTaskOutput>(index, input, output) {
    fun run(): ComputeFibonacciTask =
        ComputeFibonacciTask(
            index,
            input,
            FibonacciTaskOutput(
                Fibonacci.count(input.raw)
            )
        )
}