package core

import advanced.DaemonThreadFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException

abstract class AbstractActor<T>(protected val id: String) : Actor<T> {

    private val executor: ExecutorService = Executors.newSingleThreadExecutor(DaemonThreadFactory())

    override val context: ActorContext<T> = ActorContext(
        object : MessageProcessor<T> {

            override fun process(message: T, sender: Actor<T>) {
                onReceive(message, sender)
            }
        })

    abstract fun onReceive(message: T, sender: Actor<T>)

    override fun shutdown() {
        this.executor.shutdown()
    }

    @Synchronized
    override fun tell(message: T, sender: Actor<T>) {
        executor.execute {
            try {
                context.behavior.process(message, sender)
            } catch (e: RejectedExecutionException) {
                /*
                 * This is probably normal and means all pending tasks
                 * were canceled because the actor was stopped.
                 */
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}