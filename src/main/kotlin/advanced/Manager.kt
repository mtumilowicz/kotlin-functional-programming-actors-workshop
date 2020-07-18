package advanced

import core.AbstractActor
import core.Actor
import core.MessageProcessor

class Manager(
    id: String, list: List<Int>,
    private val client: Actor<List<Int>>,
    private val workers: Int
) : AbstractActor<Pair<Int, Int>>(id) {

    private val initial: List<Pair<Int, Int>>
    private val workList: List<Pair<Int, Int>>
    private val resultHeap: List<Pair<Int, Int>>
    private val managerFunction: (Manager) -> (Behavior) -> (Pair<Int, Int>) -> Unit

    init {
        val splitLists = list.zip(0..list.size).chunked(this.workers)
        this.initial = splitLists[0]
        this.workList = splitLists.drop(1).flatten()
        this.resultHeap = listOf()

        managerFunction = { manager ->
            { behavior ->
                { p ->
                    val result: List<Pair<Int, Int>> = behavior.resultHeap + p
                    if (result.size == list.size) {
                        this.client.tell(result.sortedBy { it.second }.map { it.first })
                    } else {
                        manager.context
                            .become(Behavior(behavior.workList.drop(1), result))
                    }
                }
            }
        }
    }

    fun start() {
        onReceive(Pair(0, 0), self())
        initial.map { this.initWorker(it)() }
    }

    private fun initWorker(t: Pair<Int, Int>): () -> Unit =
        { Worker("Worker " + t.second).tell(Pair(t.first, t.second), self()) }

    override fun onReceive(message: Pair<Int, Int>, sender: Actor<Pair<Int, Int>>) {
        context.become(Behavior(workList, resultHeap))
    }

    internal inner class Behavior
    internal constructor(
        internal val workList: List<Pair<Int, Int>>,
        internal val resultHeap: List<Pair<Int, Int>>
    ) :
        MessageProcessor<Pair<Int, Int>> {

        override fun process(
            message: Pair<Int, Int>,
            sender: Actor<Pair<Int, Int>>
        ) {
            managerFunction(this@Manager)(this@Behavior)(message)
            workList.take(1).forEach { sender.tell(it, self()) }
        }
    }
}
