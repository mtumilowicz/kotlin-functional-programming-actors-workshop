package advanced

import core.Task
import core.TaskIndex
import core.TaskInput

class ComputeFibonacciTask(
    index: TaskIndex,
    input: TaskInput
) : Task<Int>(index, input)