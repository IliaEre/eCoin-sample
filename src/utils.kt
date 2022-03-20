import UT.getUTXO
import java.math.BigInteger
import java.security.*
import java.util.*

object WalletUtils {

    fun TransactionOutput.isMine(me: PublicKey): Boolean = recipient == me

    fun Wallet.getTransactions() = getUTXO().filterValues { it.isMine(publicKey) }.values

    fun Wallet.sendFundsTo(recipient: PublicKey, amountToSend: Long): Transaction {
        require(balance > amountToSend) { "Insufficient funds" }

        val tx = Transaction.create(sender = publicKey, recipient = publicKey, amount = amountToSend)
        tx.outputs.add(TransactionOutput(recipient = recipient, amount = amountToSend, transactionHash = tx.hash))

        var collectedAmount = 0L

        for (myTx in this.getTransactions()) {
            collectedAmount += myTx.amount
            tx.inputs.add(myTx)

            if (collectedAmount > amountToSend) {
                val change = collectedAmount - amountToSend
                tx.outputs.add(TransactionOutput(recipient = publicKey, amount = change, transactionHash = tx.hash))
            }

            if (collectedAmount >= amountToSend) {
                break
            }
        }
        return tx.sign(privateKey)
    }

}

object BlockUtils {

    private const val DEFAULT_SIGNATURE_NUM = 1
    private const val DEFAULT_ALGORITHM = "SHA-256"

    fun String.hash(algorithm: String = DEFAULT_ALGORITHM): String {
        val messageDigest = MessageDigest.getInstance(algorithm)
        messageDigest.update(this.toByteArray())
        return String.format("%064x", BigInteger(DEFAULT_SIGNATURE_NUM, messageDigest.digest()))
    }

    /** Class block has the same method by init, but here we should use it again for validation reason */
    fun Block.calculateHash(): String = "$index$transactions$timestamp$blockRecord$previousHash$nonce".hash()

    /** Check the block was mined by prefix */
    fun Block.isMined(prefix: String): Boolean = this.hash.startsWith(prefix)

    fun Block.isValid(oldBlock: Block): Boolean = when {
        (oldBlock.index + 1 != this.index) || (oldBlock.hash != this.previousHash)
                || (this.calculateHash() != this.hash) -> false
        else -> true
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
