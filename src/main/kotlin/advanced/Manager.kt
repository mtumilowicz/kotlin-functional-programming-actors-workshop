package advanced

import core.AbstractActor
import core.Actor
import core.MessageProcessor
import core.TaskIndex

class Manager(
    id: String,
    list: List<Int>,
    private val client: Actor<List<Int>>,
    private val workers: Int
) : AbstractActor<Pair<Int, TaskIndex>>(id) {

    private val processing: List<Pair<Int, TaskIndex>>
    private val waiting: List<Pair<Int, TaskIndex>>
    private val results: List<Pair<Int, TaskIndex>>
    private val managerFunction: (Manager) -> (Behaviour) -> (Pair<Int, TaskIndex>) -> Unit

    init {
        val numberedList = list.zip(0..list.size).map { Pair(it.first, TaskIndex(it.second)) }
        this.processing = numberedList.take(this.workers)
        this.waiting = numberedList.drop(workers)
        this.results = listOf()

        managerFunction = { manager ->
            { behaviour ->
                { p ->
                    val result: List<Pair<Int, TaskIndex>> = behaviour.results + p
                    if (result.size == list.size) {
                        this.client.receive(result.sortedBy { it.second }.map { it.first })
                    } else {
                        manager.context
                            .become(Behaviour(behaviour.waiting.drop(1), result))
                    }
                }
            }
        }
    }

    fun start() {
        onReceive(Pair(0, TaskIndex(0)), self())
        processing.map { this.initWorker(it)() }
    }

    private fun initWorker(t: Pair<Int, TaskIndex>): () -> Unit =
        { Worker("Worker " + t.second).receive(Pair(t.first, t.second), self()) }

    override fun onReceive(message: Pair<Int, TaskIndex>, sender: Actor<Pair<Int, TaskIndex>>) {
        context.become(Behaviour(waiting, results))
    }

    internal inner class Behaviour
    internal constructor(
        internal val waiting: List<Pair<Int, TaskIndex>>,
        internal val results: List<Pair<Int, TaskIndex>>
    ) :
        MessageProcessor<Pair<Int, TaskIndex>> {

        override fun process(
            message: Pair<Int, TaskIndex>,
            sender: Actor<Pair<Int, TaskIndex>>
        ) {
            managerFunction(this@Manager)(this@Behaviour)(message)
            waiting.take(1).forEach { sender.receive(it, self()) }
        }
    }
}
