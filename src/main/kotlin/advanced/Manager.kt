package advanced

import core.AbstractActor
import core.Actor
import core.MessageProcessor

class Manager(
    id: String,
    list: List<Int>,
    private val client: Actor<List<Int>>,
    private val workers: Int
) : AbstractActor<Pair<Int, Int>>(id) {

    private val processing: List<Pair<Int, Int>>
    private val waiting: List<Pair<Int, Int>>
    private val results: List<Pair<Int, Int>>
    private val managerFunction: (Manager) -> (Behaviour) -> (Pair<Int, Int>) -> Unit

    init {
        val numberedList = list.zip(0..list.size)
        this.processing = numberedList.take(this.workers)
        this.waiting = numberedList.drop(workers)
        this.results = listOf()

        managerFunction = { manager ->
            { behaviour ->
                { p ->
                    val result: List<Pair<Int, Int>> = behaviour.results + p
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
        onReceive(Pair(0, 0), self())
        processing.map { this.initWorker(it)() }
    }

    private fun initWorker(t: Pair<Int, Int>): () -> Unit =
        { Worker("Worker " + t.second).receive(Pair(t.first, t.second), self()) }

    override fun onReceive(message: Pair<Int, Int>, sender: Actor<Pair<Int, Int>>) {
        context.become(Behaviour(waiting, results))
    }

    internal inner class Behaviour
    internal constructor(
        internal val waiting: List<Pair<Int, Int>>,
        internal val results: List<Pair<Int, Int>>
    ) :
        MessageProcessor<Pair<Int, Int>> {

        override fun process(
            message: Pair<Int, Int>,
            sender: Actor<Pair<Int, Int>>
        ) {
            managerFunction(this@Manager)(this@Behaviour)(message)
            waiting.take(1).forEach { sender.receive(it, self()) }
        }
    }
}
