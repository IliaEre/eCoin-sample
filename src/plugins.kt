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

/** Chain routing */
fun Application.chainRouting() {
    routing {
        route("/wallet") {
            get("/balance") {
                val username = getParam("username")
                MongoClient.findUserByUsername(username)
                    ?.let { call.respond(it.balance) }
                    ?: call.respond(HttpStatusCode.BadRequest, "User not found!")
            }

            post("/transfer") {
                val record = call.receive<Record>()

                // todo: move to db
                val wallet1 = PreparingFactory.mainWallet
                val wallet2 = Wallet.create(Chain)

                log.info("Chain:${Chain.getChain()}")
                log.info("Wallet1 balance: ${wallet1.balance}")
                log.info("Wallet2 balance: ${wallet2.balance}")

                val tx2 = wallet1.sendFundsTo(recipient = wallet2.publicKey, amountToSend = 33)
                Chain.add(record, tx2)

                log.info("Chain:${Chain.getChain()}")
                log.info("Wallet1 balance: ${wallet1.balance}")
                log.info("Wallet2 balance: ${wallet2.balance}")
            }
        }

        get("/chain") { call.respond(Chain.getChain()) }
    }
}

/** Health check */
fun Application.stateRouting() {
    routing { get("/healthCheck") { call.respond(mapOf("status" to "ok")) } }
}