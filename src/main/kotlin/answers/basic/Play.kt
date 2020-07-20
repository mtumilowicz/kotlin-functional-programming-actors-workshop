package answers.basic

fun main() {
    val referee = Referee()
    val player1 = Player("Player1", "Ping", referee)
    val player2 = Player("Player2", "Pong", referee)
    player1.receive(1, player2)
    Thread.sleep(100)
}