package answers.advanced

import answers.actor.AbstractActor
import answers.actor.Actor
import java.util.concurrent.Semaphore
import kotlin.streams.toList

private val semaphore = Semaphore(1)
private const val taskCount = 1_000L
private const val workersNo = 2
private val rnd = java.util.Random(0)
private val taskInputs = rnd.ints(0, 35)
    .limit(taskCount)
    .toList()

fun main() {
    semaphore.acquire()
    val startTime = System.currentTimeMillis()
    val client = object : AbstractActor<List<FibonacciTaskOutput>>("Client") {
        override fun onReceive(message: List<FibonacciTaskOutput>, sender: Actor<List<FibonacciTaskOutput>>) {
            processSuccess(message)
            println("Total time: " + (System.currentTimeMillis() - startTime))
            semaphore.release()
        }
    }
    val manager = Manager(
        id = "Manager",
        taskInputs = taskInputs,
        workers = workersNo,
        client = client
    )
    manager.start()
    semaphore.acquire()
}

fun processSuccess(lst: List<FibonacciTaskOutput>) {
    val paired = taskInputs.take(40).zip(lst.take(40).map { it.raw })
    val correct = paired.find { Fibonacci.count(it.first) != it.second } == null
    require(correct)
    println("Results: $paired")
}