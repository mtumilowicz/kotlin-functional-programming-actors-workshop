package core

class ActorContext<T>(behavior: MessageProcessor<T>) {

    var behavior: MessageProcessor<T> = behavior
        private set

    @Synchronized
    fun become(behavior: MessageProcessor<T>) {
        this.behavior = behavior
    }
}