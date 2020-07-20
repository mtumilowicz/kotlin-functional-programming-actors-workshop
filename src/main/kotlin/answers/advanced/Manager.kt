package answers.advanced

import answers.actor.AbstractActor
import answers.actor.Actor
import answers.actor.MessageProcessor
import common.task.IntTaskInput
import common.task.TaskIndex

class Manager(
    id: String,
    list: List<Int>,
    workers: Int,
    private val client: Actor<List<FibonacciTaskOutput>>
) : AbstractActor<ComputeFibonacciTask>(id) {

    private val processing: List<ComputeFibonacciTask>
    private val waiting: List<ComputeFibonacciTask>
    private val results: List<ComputeFibonacciTask>
    private val processTask: (Behaviour) -> (ComputeFibonacciTask) -> Unit

    init {
        val numberedList =
            list.zip(0..list.size)
                .map {
                    ComputeFibonacciTask(
                        TaskIndex(
                            it.second
                        ), IntTaskInput(it.first)
                    )
                }
        this.processing = numberedList.take(workers)
        this.waiting = numberedList.drop(workers)
        this.results = listOf()
        context.become(Behaviour(waiting, results))

        processTask = { behaviour ->
            { result ->
                val results: List<ComputeFibonacciTask> = behaviour.results + result
                if (results.size == list.size) {
                    this.client.enqueue(results.sortedBy { it.index }.map { it.output!! })
                } else {
                    this.context.become(Behaviour(behaviour.waiting.drop(1), results))
                }
            }
        }
    }

    fun start() {
        processing.forEach { this.startWorker(it) }
    }

    private fun startWorker(task: ComputeFibonacciTask) = Worker(
        "Worker " + task.index
    ).enqueue(task, self())

    override fun onReceive(message: ComputeFibonacciTask, sender: Actor<ComputeFibonacciTask>) {
        context.become(Behaviour(waiting, results))
    }

    internal inner class Behaviour internal constructor(
        internal val waiting: List<ComputeFibonacciTask>,
        internal val results: List<ComputeFibonacciTask>
    ) : MessageProcessor<ComputeFibonacciTask> {

        override fun process(message: ComputeFibonacciTask, sender: Actor<ComputeFibonacciTask>) {
            processTask(this)(message)
            waiting.take(1).forEach { sender.enqueue(it, self()) }
        }
    }
}
