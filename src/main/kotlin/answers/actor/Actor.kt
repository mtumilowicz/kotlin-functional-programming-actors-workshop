package answers.actor

interface Actor<T> {

    val context: ActorContext<T>

    fun self(): Actor<T> = this

    fun receive(message: T, sender: Actor<T> = self()) // used to send a message to this actor

    fun shutdown()
}