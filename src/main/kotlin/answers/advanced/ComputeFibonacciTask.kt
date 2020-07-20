package answers.advanced

import answers.core.task.IntTaskInput
import answers.core.task.Task
import answers.core.task.TaskIndex

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