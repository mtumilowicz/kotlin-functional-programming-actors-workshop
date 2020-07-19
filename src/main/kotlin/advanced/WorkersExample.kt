package advanced

import core.AbstractActor
import core.Actor
import core.IntTaskInput
import java.util.concurrent.Semaphore
import kotlin.streams.toList


private val semaphore = Semaphore(1)
private const val listLength = 1_000L
private const val workers = 2
private val rnd = java.util.Random(0)
private val testList = rnd.ints(0, 35).limit(listLength).toList()

fun main() {
    semaphore.acquire()
    val startTime = System.currentTimeMillis()
    val client = object : AbstractActor<List<IntTaskInput>>("Client") {
            override fun onReceive(message: List<IntTaskInput>, sender: Actor<List<IntTaskInput>>) {
                processSuccess(message)
                println("Total time: " + (System.currentTimeMillis() - startTime))
                semaphore.release()
            }
        }
    val manager = Manager("Manager", testList, client, workers)
    manager.start()
    semaphore.acquire()
}

fun processSuccess(lst: List<IntTaskInput>) {
    val paired = testList.take(40).zip(lst.take(40))
    val correct = paired.find { Fibonacci.count(it.first) != it.second.raw} == null
    require(correct)
    println("Results: ${testList.take(40).zip(lst.take(40).map { it.raw })}")
}