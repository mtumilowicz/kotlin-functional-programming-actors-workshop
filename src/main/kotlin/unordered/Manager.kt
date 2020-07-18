package unordered

import common.ListK
import common.Result
import common.sequence

class Manager(id: String, list: ListK<Int>,
              private val client: Actor<Result<ListK<Int>>>,
              private val workers: Int) : AbstractActor<Int>(id) {

    private val initial: ListK<Pair<Int, Int>>
    private val workList: ListK<Int>
    private val resultList: ListK<Int>
    private val managerFunction: (Manager) -> (Behavior) -> (Int) -> Unit

    init {
        val splitLists = list.splitAt(this.workers)
        this.initial = splitLists.first.zipWithPosition()
        this.workList = splitLists.second
        this.resultList = ListK()

        managerFunction = { manager ->
            { behavior ->
                { i ->
                    val result = behavior.resultList.cons(i)
                    if (result.length == list.length) {
                        this.client.tell(Result(result))
                    } else {
                        manager.context
                            .become(Behavior(behavior.workList
                                                 .tailSafe()
                                                 .getOrElse(ListK()), result))
                    }
                }
            }
        }
    }

    fun start() {
        onReceive(0, self())
        sequence(initial.map { this.initWorker(it) })
            .forEach(onSuccess = { this.initWorkers(it) },
                     onFailure = { this.tellClientEmptyResult(it.message ?: "Unknown error") })
    }

    private fun initWorker(t: Pair<Int, Int>): Result<() -> Unit> {
        // Don't follow IntelliJ advice to move the following lambda out of the parentheses, as of Kotlin 1.3.71,
        // or it will no longer compile with "Error:(48, 18) Kotlin: Type mismatch: inferred type is Unit but Boolean was expected"
        // error message.
        return Result ({ Worker("Worker " + t.second).tell(t.first, self()) })
    }

    private fun initWorkers(lst: ListK<() -> Unit>) {
        lst.forEach { it() }
    }

    private fun tellClientEmptyResult(string: String) {
        client.tell(Result.failure("$string caused by empty input list."))
    }

    override fun onReceive(message: Int, sender: Result<Actor<Int>>) {
        context.become(Behavior(workList, resultList))
    }

    internal inner class Behavior internal constructor(internal val workList: ListK<Int>,
                                                       internal val resultList: ListK<Int>) : MessageProcessor<Int> {

        override fun process(message: Int, sender: Result<Actor<Int>>) {
            managerFunction(this@Manager)(this@Behavior)(message)
            sender.forEach(onSuccess = { a: Actor<Int> ->
                workList.headSafe().forEach({ a.tell(it, self()) }) { a.shutdown() }
            })
        }
    }}
