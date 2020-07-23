package answers.basic

fun main() {
    val referee = Referee()
    val player1 = Player("Ping",  referee)
    val player2 = Player("Pong",  referee)
    player1.enqueue(1, player2)
    Thread.sleep(100)
}