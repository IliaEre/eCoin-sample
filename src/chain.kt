import BlockUtils.calculateHash
import BlockUtils.isMined
import BlockUtils.isValid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object Chain {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    /** nonce */
    private const val DIFFICULTY = 2
    private val validPrefix = "0".repeat(DIFFICULTY)
    /** transactions */
    val UTXO: ConcurrentHashMap<String, TransactionOutput> = ConcurrentHashMap()
    /** Own chain list */
    private val chain: CopyOnWriteArrayList<Block> = CopyOnWriteArrayList<Block>()
    private val lock = ReentrantLock()

    init {
        logger.info("init chain...")
    }

    /** Return copy of chain list */
    fun getChain() = CopyOnWriteArrayList(chain)

    fun add(blockRecord: BlockRecord, tx: Transaction) {
        require(tx.isSignatureValid())
        val block = generate(chain.last(), blockRecord, tx)

        require(block.isValid(chain.last())) { "Invalid Block!" }

        val minedBlock = if (block.isMined(validPrefix)) block else block.mine(validPrefix)
        chain.add(minedBlock)

        require(isValid()) { "Invalid BlockChain!" }
    }

    fun addGenesisBlock(blockRecord: BlockRecord, tx: Transaction) {
        require(tx.isSignatureValid())
        val genesisBlock = Block(
            index = 0, blockRecord = blockRecord, previousHash = "", nonce = 0, transactions = mutableListOf(tx)
        )

        val minedBlock = if (genesisBlock.isMined(validPrefix)) genesisBlock else genesisBlock.mine(validPrefix)
        chain.add(minedBlock)

        require(isValid()) { "Invalid BlockChain!" }
    }

    private fun generate(block: Block, blockRecord: BlockRecord, tx: Transaction): Block =
        Block(
            index = block.index + 1,
            blockRecord = blockRecord,
            previousHash = block.hash,
            transactions = mutableListOf(tx)
        )

    /** Validate all elements on chain */
    private fun isValid(): Boolean {
        if (chain.size == 1) { return true }
        lock.withLock {
            val chain = CopyOnWriteArrayList(chain)
            for (index in 1 until chain.size) {
                if (chain[index].isValid(chain[index -1]).not()) {
                    return false
                }
            }
        }
        return true
    }

    private fun Block.mine(prefix: String): Block {
        var block = this.copy()
        while (block.isMined(prefix).not()) {
            block = block.copy(nonce = block.nonce + 1)
        }
        updateUTXO(block)
        return block
    }

    private fun updateUTXO(block: Block) {
        block.transactions.flatMap { it.inputs }.map { it.hash }.forEach { UTXO.remove(it) }
        UTXO.putAll(block.transactions.flatMap { it.outputs }.associateBy { it.hash })
    }

}
