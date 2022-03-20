import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

object UT {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    /** transactions */
    private val UTXO: ConcurrentHashMap<String, TransactionOutput> = ConcurrentHashMap()
    private val lock = ReentrantReadWriteLock()

    fun getUTXO(): HashMap<String, TransactionOutput> = lock.readLock().withLock { HashMap(UTXO) }

    fun updateUTXO(block: Block) {
        lock.writeLock().withLock {
            block.transactions.flatMap { it.inputs }.map { it.hash }.forEach { UTXO.remove(it) }
            UTXO.putAll(block.transactions.flatMap { it.outputs }.associateBy { it.hash })
        }
    }
}
