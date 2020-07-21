package workshop.actor

/**
 *
 * use ExecutorService, Actor ~ 1 Thread, hint: Executors.newSingleThreadExecutor, DaemonThreadFactory()
 * shutdown(), hint: executor.shutdown()
 * enqueue(...), hint: executor.execute, context.behaviour.process
 * * should be synchronized to ensure that messages are processed one at a time
 * business processing: handle(message: T, sender: Actor<T>) (abstract)
 * context, hint: anonymous class implementing MessageProcessor, invoke handle method
 */
abstract class AbstractActorWorkshop<T>(protected val id: String) : ActorWorkshop<T> {

}