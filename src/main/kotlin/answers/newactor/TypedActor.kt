package answers.newactor

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicInteger

interface TypedActor {

    fun interface Behavior<T> : (T) -> Behavior<T>
    fun interface Address<T> {
        fun tell(msg: T)
    }

    class System(val executor: ExecutorService) {
        fun <T> actorOf(initial: (Address<T>) -> Behavior<T>): Address<T> {
            return object : Address<T>, Runnable {
                val on: AtomicInteger = AtomicInteger(0)

                // Our awesome little mailbox, free of blocking and evil
                val mbox = ConcurrentLinkedQueue<T>()
                var behavior = initial(this)
                override fun tell(msg: T) {
                    // Enqueue the message onto the mailbox and try to schedule for execution
                    mbox.offer(msg)
                    loop()
                }

                // Switch ourselves off, and then see if we should be rescheduled for execution
                override fun run() {
                    try {
                        if (on.get() == 1) {
                            val m = mbox.poll()
                            if (m != null) behavior = behavior(m)
                        }
                    } finally {
                        on.set(0)
                        loop()
                    }
                }

                // If there's something to process, and we're not already scheduled
                fun loop() {
                    if (!mbox.isEmpty() && on.compareAndSet(0, 1)) {
                        // Schedule to run on the Executor and back out on failure
                        try {
                            executor.execute(this)
                        } catch (t: Throwable) {
                            on.set(0)
                            throw t
                        }
                    }
                }
            }
        }

        fun shutdown() = executor.shutdown()
    }

    companion object {
        fun <T> Become(next: Behavior<T>): Behavior<T> = next
    }
}