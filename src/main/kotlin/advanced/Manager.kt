package advanced

import core.*

class Manager(
    id: String,
    list: List<Int>,
    private val client: Actor<List<FibonacciTaskOutput>>,
    workers: Int
) : AbstractActor<ComputeFibonacciTask>(id) {

    private val processing: List<ComputeFibonacciTask>
    private val waiting: List<ComputeFibonacciTask>
    private val results: List<ComputeFibonacciTask>
    private val processTask: (Behaviour) -> (ComputeFibonacciTask) -> Unit

    init {
        val numberedList =
            list.zip(0..list.size)
                .map { ComputeFibonacciTask(TaskIndex(it.second), IntTaskInput(it.first)) }
        this.processing = numberedList.take(workers)
        this.waiting = numberedList.drop(workers)
        this.results = listOf()

        processTask = { behaviour ->
            { result ->
                val results: List<ComputeFibonacciTask> = behaviour.results + result
                if (results.size == list.size) {
                    this.client.receive(results.sortedBy { it.index }.map { it.output!! })
                } else {
                    this.context.become(Behaviour(behaviour.waiting.drop(1), results))
                }
            }
        }
    }

    fun start() {
        context.become(Behaviour(waiting, results))
        processing.forEach { this.startWorker(it) }
    }

    private fun startWorker(t: ComputeFibonacciTask) = Worker("Worker " + t.index).receive(t, self())

    override fun onReceive(message: ComputeFibonacciTask, sender: Actor<ComputeFibonacciTask>) {
        context.become(Behaviour(waiting, results))
    }

    internal inner class Behaviour internal constructor(
        internal val waiting: List<ComputeFibonacciTask>,
        internal val results: List<ComputeFibonacciTask>
    ) : MessageProcessor<ComputeFibonacciTask> {

        override fun process(message: ComputeFibonacciTask, sender: Actor<ComputeFibonacciTask>) {
            processTask(this)(message)
            waiting.take(1).forEach { sender.receive(it, self()) }
        }
    }
}
