import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory

fun main() {
    QuizboardServer().start()
}


class QuizboardServer {
    private val logger = LoggerFactory.getLogger("Quizboard")
    private val quizBoard = Quizboard()

    fun start(){
        embeddedServer(Netty, port = 8081, host = "0.0.0.0") {
            routing {
                get("/") {
                    call.respondText("Hello World!")
                    logger.info("Received connection")
                }
                get("/teams") {
                    call.respondText(quizBoard.getTeams().toString())
                }
                get("/results") {
                    if (quizBoard.getTeams().isNotEmpty()){
                        call.respondText(quizBoard.getResults(quizBoard.getTeams().random()))
                    }
                    else {
                        call.respondText("No results to display")
                    }

                }
            }
        }.start(wait = true)
    }
}


