import io.ktor.application.*

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    plugins()
    chainRouting()
    stateRouting()

    Bootstrap().load()
}
