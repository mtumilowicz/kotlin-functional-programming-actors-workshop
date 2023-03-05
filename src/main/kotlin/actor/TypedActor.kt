package actor

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean


fun interface Behaviour<T> : (T) -> Behaviour<T>
fun interface ActorRef<T> {
    fun tell(msg: T)
}

class ActorSystem(val executor: ExecutorService) {
    fun <T> spawn(initial: (ActorRef<T>) -> Behaviour<T>): ActorRef<T> {
        return object : ActorRef<T> {
            val isProcessing = AtomicBoolean()

            val mailbox = ConcurrentLinkedQueue<T>()
            var behaviour = initial(this)
            override fun tell(msg: T) {
                mailbox.offer(msg)
                process()
            }

            fun run() {
                try {
                    val m = mailbox.poll()
                    if (m != null) behaviour = behaviour(m)
                } finally {
                    isProcessing.set(false)
                    process()
                }
            }

            fun process() {
                if (!mailbox.isEmpty() && isProcessing.compareAndSet(false, true)) {
                    try {
                        executor.execute { run() }
                    } catch (t: Throwable) {
                        isProcessing.set(false)
                        throw t
                    }
                }
            }
        }
    }

    fun shutdown() = executor.shutdown()
}