import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

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
fun Application.serviceRouting() {
    routing {
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