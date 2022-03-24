import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import repo.WalletBuilder

class Bootstrap {

    private val logger: Logger = LoggerFactory.getLogger(Bootstrap::class.java)

    fun load() {
        val amount = 1_000_000_000L

        logger.info("init chain...")
        val blockRecord = BlockRecord("admin", 0, "", "", amount)

        logger.info("init admin wallet")
        val mainWallet = WalletBuilder.create()
        runBlocking { MongoClient.create(WalletCreateRequest("admin"), mainWallet, amount) }

        logger.info("init first TX...")
        val tx = Transaction.create(sender = mainWallet.publicKey, recipient = mainWallet.publicKey, amount = amount)
        tx.outputs.add(TransactionOutput(recipient = mainWallet.publicKey, amount = amount, transactionHash = tx.hash))
        tx.sign(mainWallet.privateKey)

        Chain.addGenesisBlock(blockRecord, tx)

        logger.info("init was finished... size:${Chain.getChain().size}")
        logger.info("admin balance: ${mainWallet.balance}")
    }

}
