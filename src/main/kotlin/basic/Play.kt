package basic

import java.util.concurrent.Semaphore

private val semaphore = Semaphore(1)
fun main(args: Array<String>) {
    val referee = object : AbstractActor<Int>("Referee") {
        override fun onReceive(message: Int, sender: Result<Actor<Int>>) {
            println("Game ended after $message shots")
            semaphore.release()
        }
    }
    val player1 =
        Player("Player1", "Ping", referee)
    val player2 = Player("Player2", "Pong", referee)
    semaphore.acquire()
    player1.tell(1, Result.success(player2))
    semaphore.acquire()
// main thread terminates
}

private class Player(
    id: String,
    private val sound: String,
    private val referee: Actor<Int>
) :
    AbstractActor<Int>(id) {
    override fun onReceive(message: Int, sender: Result<Actor<Int>>) {
        println("$sound - $message")
        if (message >= 10) {
            referee.tell(message, sender)
        } else {
            sender.fold(
                { actor: Actor<Int> ->
                    actor.tell(message + 1, self())
                },
                { referee.tell(message, sender) }
            )
        }
    }
}