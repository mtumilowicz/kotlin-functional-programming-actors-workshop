package answers.advanced

import answers.actor.AbstractActor
import answers.actor.Actor
import common.fibonacci.Fibonacci
import common.fibonacci.FibonacciTaskOutput
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.streams.toList

class ManagerTest : BehaviorSpec({

    given("fsm") {
        val latch = CountDownLatch(1)
        val taskCount = 1_000L
        val workersNo = 2
        val rnd = java.util.Random(0)
        val taskInputs = rnd.ints(0, 35)
            .limit(taskCount)
            .toList()
        var outputs = listOf<FibonacciTaskOutput>()

        val startTime = System.currentTimeMillis()
        val client = object : AbstractActor<List<FibonacciTaskOutput>>("Client") {
            override fun handle(message: List<FibonacciTaskOutput>, sender: Actor<List<FibonacciTaskOutput>>) {
                outputs = message
                println("Total time: " + (System.currentTimeMillis() - startTime))
                latch.countDown()
            }
        }
        val manager = Manager(
            id = "Manager",
            taskInputs = taskInputs,
            workers = workersNo,
            client = client
        )
        manager.start()
        latch.await(5, TimeUnit.SECONDS)

        then("I could move from state to state") {
            val results = outputs.map { it.raw }
            val correct = taskInputs.map { Fibonacci.count(it) }

            results shouldBe correct
        }
    }
})