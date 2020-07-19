package advanced

import core.*

class Manager(
    id: String,
    list: List<Int>,
    private val client: Actor<List<TaskInput>>,
    private val workers: Int
) : AbstractActor<Pair<TaskInput, TaskIndex>>(id) {

    private val processing: List<Pair<TaskInput, TaskIndex>>
    private val waiting: List<Pair<TaskInput, TaskIndex>>
    private val results: List<Pair<TaskInput, TaskIndex>>
    private val managerFunction: (Manager) -> (Behaviour) -> (Pair<TaskInput, TaskIndex>) -> Unit

    init {
        val numberedList = list.zip(0..list.size).map { Pair(TaskInput(it.first), TaskIndex(it.second)) }
        this.processing = numberedList.take(this.workers)
        this.waiting = numberedList.drop(workers)
        this.results = listOf()

        managerFunction = { manager ->
            { behaviour ->
                { p ->
                    val result: List<Pair<TaskInput, TaskIndex>> = behaviour.results + p
                    if (result.size == list.size) {
                        this.client.receive(result.sortedBy { it.second }.map { it.first })
                    } else {
                        manager.context.become(Behaviour(behaviour.waiting.drop(1), result))
                    }
                }
            }
        }
    }

    fun start() {
        onReceive(Pair(TaskInput(0), TaskIndex(0)), self())
        processing.map { this.initWorker(it)() }
    }

    private fun initWorker(t: Pair<TaskInput, TaskIndex>): () -> Unit =
        { Worker("Worker " + t.second).receive(Pair(t.first, t.second), self()) }

    override fun onReceive(message: Pair<TaskInput, TaskIndex>, sender: Actor<Pair<TaskInput, TaskIndex>>) {
        context.become(Behaviour(waiting, results))
    }

    internal inner class Behaviour
    internal constructor(
        internal val waiting: List<Pair<TaskInput, TaskIndex>>,
        internal val results: List<Pair<TaskInput, TaskIndex>>
    ) :
        MessageProcessor<Pair<TaskInput, TaskIndex>> {

        override fun process(
            message: Pair<TaskInput, TaskIndex>,
            sender: Actor<Pair<TaskInput, TaskIndex>>
        ) {
            managerFunction(this@Manager)(this@Behaviour)(message)
            waiting.take(1).forEach { sender.receive(it, self()) }
        }
    }
}
