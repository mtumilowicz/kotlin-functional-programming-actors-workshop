package workshop.actor

interface MessageProcessor<T> {

    fun process(message: T, sender: Actor<T>)
}