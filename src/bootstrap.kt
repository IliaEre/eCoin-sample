import org.slf4j.Logger
import org.slf4j.LoggerFactory

object PreparingFactory {

    val mainWallet = Wallet.create(Chain)

    fun initFirstChain() {
        logger.info("init first block...")
        val firstRecord = Record(1.0, 1L)

        logger.info("init first TX...")
        val tx = Transaction.create(sender = mainWallet.publicKey, recipient = mainWallet.publicKey, amount = AMOUNT)
        tx.outputs.add(TransactionOutput(recipient = mainWallet.publicKey, amount = 100, transactionHash = tx.hash))
        tx.sign(mainWallet.privateKey)

        logger.info("add new block to chain...")
        Chain.addFirstBlock(firstRecord, tx)

        logger.info("main block was added successfully.")
    }

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private const val AMOUNT = 1_000_000
}