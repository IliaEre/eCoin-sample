import RsaUtils.decodeToPrivateKey
import RsaUtils.decodeToPublicKey
import RsaUtils.encodeToString
import org.litote.kmongo.reactivestreams.*
import org.litote.kmongo.coroutine.*
import org.litote.kmongo.eq
import repo.WalletBuilder
import java.time.OffsetDateTime

object MongoClient {

    private const val DATABASE = "wallets"
    private val client = KMongo.createClient().coroutine
    private val db = client.getDatabase(DATABASE)

    suspend fun findBalanceByUsername(username: String): BalanceWalletDto? =
        db.getCollection<WalletEntity>().findOne(WalletEntity::username eq username)?.convertTo()

    suspend fun findByUsername(username: String): Wallet? =
        db.getCollection<WalletEntity>().findOne(WalletEntity::username eq username)?.convertToWallet()

    suspend fun create(walletCreateRequest: WalletCreateRequest, balance: Long = 0) {
        val wallet = WalletBuilder.create()
        db.getCollection<WalletEntity>().insertOne(walletCreateRequest.toEntity(wallet, balance))
    }

    suspend fun create(walletCreateRequest: WalletCreateRequest, wallet: Wallet, balance: Long = 0) {
        db.getCollection<WalletEntity>().insertOne(walletCreateRequest.toEntity(wallet, balance))
    }

    private fun WalletEntity.convertTo() = BalanceWalletDto(username, balance)

    private fun WalletEntity.convertToWallet() = Wallet(
        publicKey.decodeToPublicKey(),
        privateKey.decodeToPrivateKey(),
        Chain
    )

    private fun WalletCreateRequest.toEntity(wallet: Wallet, balance: Long) = WalletEntity(
        username,
        OffsetDateTime.now().toEpochSecond(),
        wallet.publicKey.encodeToString(),
        wallet.privateKey.encodeToString(),
        balance
    )

}
