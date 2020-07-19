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

    private val initial: List<Pair<Int, TaskIndex>>
    private val workList: List<Pair<Int, TaskIndex>>
    private val resultHeap: List<Pair<Int, TaskIndex>>
    private val managerFunction: (Manager) -> (Behaviour) -> (Pair<Int, TaskIndex>) -> Unit

    init {
        val numberedList = list.zip((0..list.size).map { TaskIndex(it) })
        val splitLists = Pair(numberedList.take(this.workers), numberedList.drop(workers))
        this.initial = splitLists.first
        this.workList = splitLists.second
        this.resultHeap = listOf()

        managerFunction = { manager ->
            { behaviour ->
                { p ->
                    val result: List<Pair<Int, TaskIndex>> = behaviour.resultHeap + p
                    if (result.size == list.size) {
                        this.client.receive(result.sortedBy { it.second }.map { it.first })
                    } else {
                        manager.context
                            .become(Behaviour(behaviour.workList.drop(1), result))
                    }
                }
            }
        }
    }

    fun start() {
        onReceive(Pair(0, TaskIndex(0)), self())
        initial.map { this.initWorker(it)() }
    }

    private fun initWorker(t: Pair<Int, TaskIndex>): () -> Unit =
        { Worker("Worker " + t.second).receive(Pair(t.first, t.second), self()) }

    override fun onReceive(message: Pair<Int, TaskIndex>, sender: Actor<Pair<Int, TaskIndex>>) {
        context.become(Behaviour(workList, resultHeap))
    }

    internal inner class Behaviour
    internal constructor(
        internal val workList: List<Pair<Int, TaskIndex>>,
        internal val resultHeap: List<Pair<Int, TaskIndex>>
    ) :
        MessageProcessor<Pair<Int, TaskIndex>> {

        override fun process(
            message: Pair<Int, TaskIndex>,
            sender: Actor<Pair<Int, TaskIndex>>
        ) {
            managerFunction(this@Manager)(this@Behaviour)(message)
            workList.take(1).forEach { sender.receive(it, self()) }
        }
    }
}
