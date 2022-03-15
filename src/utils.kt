import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.security.*
import java.util.*

object BlockUtils {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun String.hash(algorithm: String = "SHA-256"): String {
        val messageDigest = MessageDigest.getInstance(algorithm)
        messageDigest.update(this.toByteArray())
        return String.format("%064x", BigInteger(1, messageDigest.digest()))
    }

    /**
     * Class block has the same method by init, but here we should use it again for validation reason
     * */
    fun Block.calculateHash(): String = "$index$transactions$timestamp$record$previousHash$nonce".hash()

    /**
     * Check the block was mined by prefix
     * */
    fun Block.isMined(prefix: String): Boolean = this.hash.startsWith(prefix)

    fun Block.mine(prefix: String): Block {
        logger.info("Mine block with index:${this.index}")

        var block = this.copy()
        while (block.isMined(prefix).not()) {
            block = block.copy(nonce = block.nonce + 1)
        }

        logger.info("block was mined...")
        return block
    }

}

object RsaUtils {

    private const val ALGORITHM: String = "SHA256withRSA"

    private fun rsaInstance(algorithm: String = ALGORITHM): Signature = Signature.getInstance(algorithm)

    fun String.sign(privateKey: PrivateKey) : ByteArray {
        val rsa = rsaInstance()
        rsa.initSign(privateKey)
        rsa.update(this.toByteArray())
        return rsa.sign()
    }

    fun String.verifySignature(publicKey: PublicKey, signature: ByteArray) : Boolean {
        val rsa = rsaInstance()
        rsa.initVerify(publicKey)
        rsa.update(this.toByteArray())
        return rsa.verify(signature)
    }

    fun Key.encodeToString() : String = Base64.getEncoder().encodeToString(this.encoded)

}
