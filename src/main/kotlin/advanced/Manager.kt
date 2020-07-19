package advanced

import core.*

class Manager(
    id: String,
    list: List<Int>,
    private val client: Actor<List<IntTaskInput>>,
    private val workers: Int
) : AbstractActor<ComputeFibonacciTask>(id) {

    private val processing: List<ComputeFibonacciTask>
    private val waiting: List<ComputeFibonacciTask>
    private val results: List<ComputeFibonacciTask>
    private val managerFunction: (Manager) -> (Behaviour) -> (ComputeFibonacciTask) -> Unit

    init {
        val numberedList =
            list.zip(0..list.size)
                .map { ComputeFibonacciTask(TaskIndex(it.second), IntTaskInput(it.first)) }
        this.processing = numberedList.take(this.workers)
        this.waiting = numberedList.drop(workers)
        this.results = listOf()

        managerFunction = { manager ->
            { behaviour ->
                { p ->
                    val result: List<ComputeFibonacciTask> = behaviour.results + p
                    if (result.size == list.size) {
                        this.client.receive(result.sortedBy { it.index }.map { it.input })
                    } else {
                        manager.context.become(Behaviour(behaviour.waiting.drop(1), result))
                    }
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
            managerFunction(this@Manager)(this@Behaviour)(message)
            waiting.take(1).forEach { sender.receive(it, self()) }
        }
    }
}
