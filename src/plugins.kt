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
                val w2 = MongoClient.findByUsername(blockRecord.to)

                if (w1 != null) {
                    if (w2 != null) {
                        val tx2 = w1.sendFundsTo(recipient = w2.publicKey, amountToSend = blockRecord.sum)
                        Chain.add(blockRecord, tx2)
                    } else {
                        throw IllegalStateException("couldn't find 'to wallet'")
                    }
                } else {
                    throw IllegalStateException("couldn't find 'from wallet'")
                }

                log.info("Chain:${Chain.getChain()}")
                log.info("Wallet1 balance: ${w1.balance}")
                log.info("Wallet2 balance: ${w2.balance}")

                call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
            }
        }

        get("/chain") { call.respond(Chain.getChain()) }
    }
}
