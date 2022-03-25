import WalletUtils.sendFundsTo
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ktor.getParam

/** Main plugins */
fun Application.plugins() {
    install(CallLogging)
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
        }
    }
}

/** Health check */
fun Application.stateRouting() {
    routing { get("/healthCheck") { call.respond(mapOf("status" to "ok")) } }
}

/** Chain routing */
fun Application.chainRouting() {
    routing {
        route("/wallet") {

            post {
                val record = call.receive<WalletCreateRequest>()
                MongoClient.create(record)
                call.respond(HttpStatusCode.Created)
            }

            get("/balance/{username}") {
                val username = getParam("username")
                MongoClient.findBalanceByUsername(username)
                    ?.let { call.respond(it.balance) }
                    ?: call.respond(HttpStatusCode.NotFound)
            }

            post("/transfer") {
                val blockRecord = call.receive<BlockRecord>()
                val w1 = MongoClient.findByUsername(blockRecord.from)
                    ?: throw IllegalStateException("couldn't find 'from wallet'")
                val w2 = MongoClient.findByUsername(blockRecord.to)
                    ?: throw IllegalStateException("couldn't find 'to wallet'")

                val tx2 = w1.sendFundsTo(recipient = w2.publicKey, amountToSend = blockRecord.sum)
                Chain.add(blockRecord, tx2)

                call.respond(HttpStatusCode.OK, BalanceResponse("ok", w1.balance, w2.balance))
            }
        }

        get("/chain") { call.respond(Chain.getChain()) }
    }
}
