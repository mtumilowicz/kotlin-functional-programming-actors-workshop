package workshop.basic

fun main() {
    val referee = RefereeWorkshop()
    val player1 = PlayerWorkshop("Player1", referee)
    val player2 = PlayerWorkshop("Player2", referee)
    // send message to player1 from player2, initial value = 1, hint: player1.enqueue
    Thread.sleep(100)
}