package core

interface Actor<T> {

    val context: ActorContext<T>

    fun self(): Actor<T> = this

    fun tell(message: T, sender: Actor<T> = self())

    fun shutdown()
}