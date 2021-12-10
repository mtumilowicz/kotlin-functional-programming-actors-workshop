package answers.actor

class ActorContext<T>(behaviour: MessageProcessor<T>) {

    var behaviour: MessageProcessor<T> = behaviour
        private set

    @Synchronized
    fun become(behaviour: MessageProcessor<T>) {
        this.behaviour = behaviour
    }
}