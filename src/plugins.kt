import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.getParam(param: String) =
    this.call.receiveParameters()[param] ?: error("$param not found!")

/**
 * Main plugins
 * */
fun Application.plugins() {
    install(CallLogging)
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
        }
    }
}

/**
 * Chain routing
 * */
fun Application.chainRouting() {
    routing {
        route("/wallet") {
            get("/balance") {
                val username = getParam("username")
                MongoClient.findUserByUsername(username)
                    ?.let { call.respond(it.balance) }
                    ?: call.respond(HttpStatusCode.BadRequest, "User not found!")
            }
        }

        get("/chain") { call.respond(Chain.getChain()) }

        post("/chain") {
            val record = call.receive<Record>()
            Chain.add(record)
            call.respond(HttpStatusCode.Created)
        }
    }
}

/**
 * Health check
 * */
fun Application.stateRouting() {
    routing { get("/healthCheck") { call.respond(mapOf("status" to "ok")) } }
}