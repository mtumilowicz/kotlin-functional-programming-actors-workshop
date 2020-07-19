package core

class ActorContext<T>(behavior: MessageProcessor<T>) {

    var behavior: MessageProcessor<T> = behavior // access to the actor’s behavior
        private set

    @Synchronized
    fun become(behavior: MessageProcessor<T>) { // actor to change the way it processes messages (behavior)
        this.behavior = behavior
    }
}