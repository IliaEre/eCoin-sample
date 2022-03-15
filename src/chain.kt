import BlockUtils.calculateHash
import BlockUtils.isMined
import BlockUtils.mine
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object Chain {

    private const val DIFFICULTY = 5
    private val validPrefix = "0".repeat(DIFFICULTY)

    // todo
    var UTXO: MutableMap<String, TransactionOutput> = mutableMapOf()

    /**
     * Own chain list
     * */
    private val chain: CopyOnWriteArrayList<Block> = CopyOnWriteArrayList<Block>()

    init {
        chain.add(Block(index = 0, record = Record(weight = 0.0, date = 0L), previousHash = "", nonce = 0L))
    }

    private val lock = ReentrantLock()

    /**
     * Return copy of chain list
     * */
    fun getChain() = CopyOnWriteArrayList(chain)

    /**
     * Steps:
     * 1. Generate new block
     * 2. Validate last
     * 3. Add block to chain
     * 4. Validate again
     * */
    fun add(record: Record) {
        val block = generate(chain.last(), record)
        require(block.isValid(chain.last())) { "Invalid Block!" }

        val minedBlock = if (block.isMined(validPrefix)) block else block.mine(validPrefix)
        chain.add(minedBlock)

        require(isValid()) { "Invalid BlockChain!" }
    }

    /**
     * Generate new block
     * */
    private fun generate(block: Block, record: Record): Block =
        Block(index = block.index + 1, record = record, previousHash = block.hash)

    /**
     * Validate all elements on chain
     * */
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

    /**
     * Validate new block. Steps:
     * 1. index
     * 2. previous hash and old block hash
     * 3. eq current hash and recalculate one more time
     * */
    private fun Block.isValid(oldBlock: Block): Boolean = when {
            (oldBlock.index + 1 != this.index) || (oldBlock.hash != this.previousHash)
                    || (this.calculateHash() != this.hash) -> false
            else -> true
        }

    private fun updateUTXO(block: Block) {
        block.transactions.flatMap { it.inputs }.map { it.hash }.forEach { UTXO.remove(it) }
        UTXO.putAll(block.transactions.flatMap { it.outputs }.associateBy { it.hash })
    }

}
