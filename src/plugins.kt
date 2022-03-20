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
import repo.WalletRepository

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
            get("/balance/{username}") {
                val username = getParam("username")
                val balance = WalletRepository.findByUserName(username).balance
                call.respond(balance)
            }

            post("/transfer") {
                val username = call.request.headers["username"]
                require(username.isNullOrEmpty().not()) {
                    throw IllegalStateException("username must be not null.")
                }

                val blockRecord = call.receive<BlockRecord>()
                val w1 = WalletRepository.findByUserName(blockRecord.from)
                val w2 = WalletRepository.findByUserName(blockRecord.to)

                val tx2 = w1.sendFundsTo(recipient = w2.publicKey, amountToSend = blockRecord.sum)
                Chain.add(blockRecord, tx2)

                log.info("Chain:${Chain.getChain()}")
                log.info("Wallet1 balance: ${w1.balance}")
                log.info("Wallet2 balance: ${w2.balance}")

                call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
            }
        }

        get("/chain") { call.respond(Chain.getChain()) }
    }
}
