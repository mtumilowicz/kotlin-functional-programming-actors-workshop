package workshop.actor

/**
 * every ActorContext has changeable behaviour, hint: MessageProcessor
 * * provide access to the actor’s behaviour, hint: var behaviour, private set
 * * actor to change the way it processes messages (behaviour)
 * * behaviour mutation, hint: become(behaviour: MessageProcessor<T>), @Synchronized
 */
class ActorContext<T>(behaviour: MessageProcessor<T>) {

}