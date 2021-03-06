package answers.actor

import common.DaemonThreadFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class AbstractActor<T>(protected val id: String) : Actor<T> {

    private val executor: ExecutorService = Executors.newSingleThreadExecutor(DaemonThreadFactory())

    override val context: ActorContext<T> =
        ActorContext(
            object : MessageProcessor<T> {

                override fun process(message: T, sender: Actor<T>) {
                    handle(message, sender)
                }
            })

    abstract fun handle(message: T, sender: Actor<T>) // business processing

    override fun shutdown() {
        this.executor.shutdown()
    }

    @Synchronized // synchronized to ensure that messages are processed one at a time
    override fun enqueue(message: T, sender: Actor<T>) {
        executor.execute { context.behaviour.process(message, sender) }
    }
}