package common.task

open class Task<Input, Output>(
    val index: TaskIndex,
    val input: Input,
    val output: Output? = null
)