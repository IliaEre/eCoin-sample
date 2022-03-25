package extra

import Chain
import Wallet

@Deprecated("Should create logical way to create wallet here")
object WalletBuilder {
    fun create(): Wallet = Wallet.create(Chain)
}