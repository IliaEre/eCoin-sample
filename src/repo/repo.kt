package repo

import Chain
import Wallet
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

object WalletRepository {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val repo = ConcurrentHashMap<String, Wallet>()

    init {
        logger.info("init wallets")
        repo["user2"] = Wallet.create(Chain)
        repo["user3"] = Wallet.create(Chain)
        logger.info("finished...")
    }

    fun findByUserName(username: String): Wallet = repo[username] ?: throw IllegalStateException("wallet not found")

    fun create(username: String): Wallet = Wallet.create(Chain).also { repo[username] = it }

}