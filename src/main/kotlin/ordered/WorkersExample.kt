package ordered

import java.util.concurrent.Semaphore


private val semaphore = Semaphore(1)
private const val listLength = 1_000
private const val workers = 2
private val rnd = java.util.Random(0)
private val testList =
    (0..listLength).map { rnd.nextInt(35) }

fun main() {
    semaphore.acquire()
    val startTime = System.currentTimeMillis()
    val client =
        object: AbstractActor<List<Int>>("Client") {
            override fun onReceive(message: List<Int>,
                          sender: Actor<List<Int>>) {
                processSuccess(message)
                println("Total time: " + (System.currentTimeMillis() - startTime))
                semaphore.release()
            }
        }

    val manager =
        Manager("Manager", testList, client, workers)
    manager.start()
    semaphore.acquire()
}

fun processSuccess(lst: List<Int>) {
    println("Input: ${testList.chunked(40)[0]}")
    println("Result: ${lst.chunked(40)[0]}")
}
