package ordered

import common.Result
import common.sequence

class Manager(id: String, list: List<Int>,
              private val client: Actor<Result<List<Int>>>,
              private val workers: Int) : AbstractActor<Pair<Int, Int>>(id) {

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
                        this.client.tell(Result(result.sortedBy { it.second }.map { it.first }))
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
        val xxx: List<Result<() -> Unit>> = initial.map { this.initWorker(it) }
        val map = xxx.map { x -> x.getOrElse { println("a") } }
        this.initWorkers(map)
    }

    private fun initWorker(t: Pair<Int, Int>): Result<() -> Unit> =
        Result(a = { Worker("Worker " + t.second).tell(Pair(t.first, t.second), self()) })

    private fun initWorkers(lst: List<() -> Unit>) {
        lst.forEach { it() }
    }

    private fun tellClientEmptyResult(string: String) {
        client.tell(Result.failure("$string caused by empty input list."))
    }

    override fun onReceive(message: Pair<Int, Int>, sender: Result<Actor<Pair<Int, Int>>>) {
        context.become(Behavior(workList, resultHeap))
    }

    internal inner class Behavior
        internal constructor(internal val workList: List<Pair<Int, Int>>,
                             internal val resultHeap: List<Pair<Int, Int>>) :
        MessageProcessor<Pair<Int, Int>> {

        override fun process(message: Pair<Int, Int>,
                             sender: Result<Actor<Pair<Int, Int>>>) {
            managerFunction(this@Manager)(this@Behavior)(message)
            sender.forEach(onSuccess = { a: Actor<Pair<Int, Int>> ->
                workList.take(1).forEach({ a.tell(it, self()) })
            })
        }
    }
}
