package workshop.actor

/**
 * every actor should have context: ActorContext, hint: val context: ActorContext<T>
 * it should be possible to queue a message to this actor from given sender
 * * hint: enqueue(message: T, sender: Actor<T> = self())
 * every actor should have a shutdown function
 */
interface Actor<T> {

}