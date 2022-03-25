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

data class BalanceResponse(val status: String, val w1: Long, val w2: Long)
