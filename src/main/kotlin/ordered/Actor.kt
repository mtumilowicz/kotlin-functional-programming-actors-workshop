package ordered

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadFactory

class ActorContext<T>(behavior: MessageProcessor<T>) {

    var behavior: MessageProcessor<T> = behavior
        private set

    @Synchronized
    fun become(behavior: MessageProcessor<T>) {
        this.behavior = behavior
    }
}

interface MessageProcessor<T> {

    fun process(message: T, sender: Actor<T>)
}

interface Actor<T> {

    val context: ActorContext<T>

    fun self(): Actor<T> = this

    fun tell(message: T, sender: Actor<T> = self())

    fun shutdown()
}

class DaemonThreadFactory : ThreadFactory {

    override fun newThread(runnableTask: Runnable): Thread {
        val thread = Executors.defaultThreadFactory().newThread(runnableTask)
        thread.isDaemon = true
        return thread
    }
}


abstract class AbstractActor<T>(protected val id: String) : Actor<T> {

    override val context: ActorContext<T> = ActorContext(
        object : MessageProcessor<T> {

            override fun process(message: T, sender: Actor<T>) {
                onReceive(message, sender)
            }
        })

    private val executor: ExecutorService =
        Executors.newSingleThreadExecutor(DaemonThreadFactory())

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
