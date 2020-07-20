package answers.actor

@FunctionalInterface
interface MessageProcessor<T> {

    fun process(message: T, sender: Actor<T>)
}