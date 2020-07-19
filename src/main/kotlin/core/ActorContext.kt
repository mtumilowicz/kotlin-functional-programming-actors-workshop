package core

class ActorContext<T>(behaviour: MessageProcessor<T>) {

    var behaviour: MessageProcessor<T> = behaviour // access to the actor’s behaviour
        private set

    @Synchronized
    fun become(behaviour: MessageProcessor<T>) { // actor to change the way it processes messages (behaviour)
        this.behaviour = behaviour
    }
}