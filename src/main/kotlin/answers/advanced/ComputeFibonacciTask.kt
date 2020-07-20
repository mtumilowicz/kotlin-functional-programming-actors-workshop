package answers.advanced

import common.task.IntTaskInput
import common.task.Task
import common.task.TaskIndex

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