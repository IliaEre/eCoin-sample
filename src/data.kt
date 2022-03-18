import BlockUtils.calculateHash
import BlockUtils.hash
import RsaUtils.encodeToString
import RsaUtils.sign
import RsaUtils.verifySignature
import WalletUtils.isMine
import java.time.Instant
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey

/**
 * Custom data
 * */
data class BlockRecord(
    val platformName: String,
    val date: Long,
    val from: String,
    val to: String,
    val sum: Long
)

data class Block(
    val index: Int = 0,
    val transactions: MutableList<Transaction> = mutableListOf(),
    val timestamp: Long = Instant.now().toEpochMilli(),
    val blockRecord: BlockRecord,
    val previousHash: String,
    val nonce: Long = 0,
) {
    val hash: String = this.calculateHash()
}

/**
 * Client wallet
 * */
data class Wallet(val publicKey: PublicKey, val privateKey: PrivateKey, val blockChain: Chain) {

    companion object {
        fun create(blockChain: Chain): Wallet =
            KeyPairGenerator.getInstance("RSA")
                .also { it.initialize(2048) }
                .generateKeyPair()
                .let { keyPair -> Wallet(keyPair.public, keyPair.private, blockChain) }
    }

    val balance: Long
        get() {
            return getMyTransactions().sumOf { it.amount }
        }

    private fun getMyTransactions(): Collection<TransactionOutput> =
        blockChain.UTXO.filterValues { it.isMine(publicKey) }.values

    fun sendFundsTo(recipient: PublicKey, amountToSend: Long): Transaction {
        require(balance > amountToSend) { "Insufficient funds" }

        val tx = Transaction.create(sender = publicKey, recipient = publicKey, amount = amountToSend)
        tx.outputs.add(TransactionOutput(recipient = recipient, amount = amountToSend, transactionHash = tx.hash))

        var collectedAmount = 0L

        for (myTx in getMyTransactions()) {
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

data class TransactionOutput(
    val recipient: PublicKey,
    val amount: Long,
    val transactionHash: String,
    val hash: String = "${recipient.encodeToString()}$amount$transactionHash".hash()
)

data class Transaction(
    val sender: PublicKey,
    val recipient: PublicKey,
    val amount: Long,
    val inputs: MutableList<TransactionOutput> = mutableListOf(),
    val outputs: MutableList<TransactionOutput> = mutableListOf(),
    val hash: String = "${sender.encodeToString()}${recipient.encodeToString()}$amount$salt".hash()
) {

    private var signature: ByteArray = ByteArray(0)

    companion object {
        fun create(sender: PublicKey, recipient: PublicKey, amount: Long): Transaction =
            Transaction(sender, recipient, amount)

        var salt: Long = 0
            get() {
                field += 1
                return field
            }
    }

    fun sign(privateKey: PrivateKey): Transaction {
        signature = "${sender.encodeToString()}${recipient.encodeToString()}$amount".sign(privateKey)
        return this
    }

    fun isSignatureValid(): Boolean =
        "${sender.encodeToString()}${recipient.encodeToString()}$amount".verifySignature(sender, signature)

}
