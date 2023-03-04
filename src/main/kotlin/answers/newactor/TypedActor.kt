package answers.newactor

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger

interface TypedActor {

    fun interface Effect<T> : (Behavior<T>) -> Behavior<T>
    fun interface Behavior<T> : (T) -> Effect<T>
    fun interface Address<T> {
        fun tell(msg: T): Address<T>
    }

    class System(val executor: Executor) {
        fun <T> actorOf(initial: (Address<T>) -> Behavior<T>): Address<T> {
            return object : Address<T>, Runnable {
                val on: AtomicInteger = AtomicInteger(0)

                // Our awesome little mailbox, free of blocking and evil
                val mbox = ConcurrentLinkedQueue<T>()
                var behavior = initial(this)
                override fun tell(msg: T): Address<T> {
                    // Enqueue the message onto the mailbox and try to schedule for execution
                    mbox.offer(msg); async(); return this
                }

                // Switch ourselves off, and then see if we should be rescheduled for execution
                override fun run() {
                    try {
                        if (on.get() == 1) {
                            val m = mbox.poll()
                            if (m != null) behavior = behavior(m)(behavior)
                        }
                    } finally {
                        on.set(0); async(); }
                }

                // If there's something to process, and we're not already scheduled
                fun async() {
                    if (!mbox.isEmpty() && on.compareAndSet(0, 1)) {
                        // Schedule to run on the Executor and back out on failure
                        try {
                            executor.execute(this); } catch (t: Throwable) {
                            on.set(0); throw t; }
                    }
                }
            }
        }

    }

    companion object {
        fun <T> Become(next: Behavior<T>): Effect<T> = Effect { current: Behavior<T> -> next }
        fun <T> Stay(): Effect<T> = Effect { x: Behavior<T> -> x }
    }
}