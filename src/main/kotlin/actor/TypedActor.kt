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
        return object : ActorRef<T>, Runnable {
            val isProcessing = AtomicBoolean()

            val mbox = ConcurrentLinkedQueue<T>()
            var behavior = initial(this)
            override fun tell(msg: T) {
                mbox.offer(msg)
                loop()
            }

            override fun run() {
                try {
                    if (isProcessing.get()) {
                        val m = mbox.poll()
                        if (m != null) behavior = behavior(m)
                    }
                } finally {
                    isProcessing.set(false)
                    loop()
                }
            }

            fun loop() {
                if (!mbox.isEmpty() && isProcessing.compareAndSet(false, true)) {
                    try {
                        executor.execute(this)
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