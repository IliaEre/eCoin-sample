import kotlinx.serialization.Serializable

@Serializable
data class WalletEntity(
    val username: String,
    val timeStamp: Long,
    val publicKey: String,
    val privateKey: String,
    val balance: Long
)

data class BalanceWalletDto(val username: String, val balance: Long)

data class WalletCreateRequest(val username: String)
