package workshop.advanced

import answers.actor.AbstractActor
import answers.actor.Actor
import common.fibonacci.Fibonacci
import common.fibonacci.FibonacciTaskOutput
import workshop.actor.AbstractActorWorkshop
import java.util.concurrent.Semaphore
import kotlin.streams.toList

private val semaphore = Semaphore(1)
private const val taskCount = 1_000L
private const val workersNo = 2
private val rnd = java.util.Random(0)
// create taskCount random ints in range [0; 35], hint: rnd, ints, limit
private val taskInputs: List<Int> = listOf()

fun main() {
    semaphore.acquire()
    val startTime = System.currentTimeMillis()
    // client client as an anonymous class implementation, hint: object : xxx
    val client: AbstractActorWorkshop<List<FibonacciTaskOutput>> = object : AbstractActorWorkshop<List<FibonacciTaskOutput>>("Client") {
        // override handle
        // processSuccess
        // print total time
        // release semaphore
    }
    // create manager and start it, hint: ManagerWorkshop
    // acquire semaphore
}

fun processSuccess(lst: List<FibonacciTaskOutput>) {
    val paired = taskInputs.take(40).zip(lst.take(40).map { it.raw })
    val correct = paired.find { Fibonacci.count(it.first) != it.second } == null
    require(correct)
    println("Results: $paired")
}