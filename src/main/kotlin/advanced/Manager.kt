package advanced

import core.*

class Manager(
    id: String,
    list: List<Int>,
    private val client: Actor<List<IntTaskInput>>,
    workers: Int
) : AbstractActor<ComputeFibonacciTask>(id) {

    private val processing: List<ComputeFibonacciTask>
    private val waiting: List<ComputeFibonacciTask>
    private val results: List<ComputeFibonacciTask>
    private val managerFunction: (Behaviour) -> (ComputeFibonacciTask) -> Unit

    init {
        val numberedList =
            list.zip(0..list.size)
                .map { ComputeFibonacciTask(TaskIndex(it.second), IntTaskInput(it.first)) }
        this.processing = numberedList.take(workers)
        this.waiting = numberedList.drop(workers)
        this.results = listOf()

        managerFunction = { behaviour ->
            { result ->
                val results: List<ComputeFibonacciTask> = behaviour.results + result
                if (results.size == list.size) {
                    this.client.receive(results.sortedBy { it.index }.map { it.input })
                } else {
                    this.context.become(Behaviour(behaviour.waiting.drop(1), results))
                }
            }
        }
    }

    fun start() {
        onReceive(ComputeFibonacciTask(TaskIndex(0), IntTaskInput(0)), self())
        processing.map { this.initWorker(it) }.forEach { it() }
    }

    private fun initWorker(t: ComputeFibonacciTask): () -> Unit =
        { Worker("Worker " + t.index).receive(t, self()) }

    override fun onReceive(message: ComputeFibonacciTask, sender: Actor<ComputeFibonacciTask>) {
        context.become(Behaviour(waiting, results))
    }

    internal inner class Behaviour
    internal constructor(
        internal val waiting: List<ComputeFibonacciTask>,
        internal val results: List<ComputeFibonacciTask>
    ) :
        MessageProcessor<ComputeFibonacciTask> {

        override fun process(
            message: ComputeFibonacciTask,
            sender: Actor<ComputeFibonacciTask>
        ) {
            managerFunction(this@Behaviour)(message)
            waiting.take(1).forEach { sender.receive(it, self()) }
        }
    }
}
