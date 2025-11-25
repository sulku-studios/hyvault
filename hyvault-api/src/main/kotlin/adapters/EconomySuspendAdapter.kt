package fi.sulku.hytale.economy.api.adapters

import fi.sulku.hytale.economy.api.EconomyResult
import fi.sulku.hytale.economy.api.PlayerBalance
import fi.sulku.hytale.economy.api.PlayerEconomy
import fi.sulku.hytale.economy.api.BalanceChange
import fi.sulku.hytale.economy.api.Transfer
import kotlinx.coroutines.future.await
import java.math.BigDecimal
import java.util.*

/**
 * This wrapper class for kotlin users
 * adds support for suspend functions.
 */
class EconomySuspendAdapter(private val economy: PlayerEconomy) {
    val name: String get() = economy.name
    val currencyPlural: String get() = economy.currencyPlural
    val currencySingular: String get() = economy.currencySingular
    val fractionalDigits: Int get() = economy.fractionalDigits

    fun format(amount: BigDecimal): String = economy.format(amount)

    suspend fun createAccount(uuid: UUID): EconomyResult<Boolean> = economy.createAccount(uuid).await()

    suspend fun hasAccount(uuid: UUID): EconomyResult<Boolean> = economy.hasAccount(uuid).await()

    suspend fun getBalance(uuid: UUID): EconomyResult<BigDecimal> = economy.getBalance(uuid).await()

    suspend fun setBalance(uuid: UUID, amount: BigDecimal): EconomyResult<BalanceChange> = economy.setBalance(uuid, amount).await()

    suspend fun withdraw(uuid: UUID, amount: BigDecimal): EconomyResult<BalanceChange> = economy.withdraw(uuid, amount).await()

    suspend fun deposit(uuid: UUID, amount: BigDecimal): EconomyResult<BalanceChange> = economy.deposit(uuid, amount).await()

    suspend fun transfer(from: UUID, to: UUID, amount: BigDecimal): EconomyResult<Transfer> = economy.transfer(from, to, amount).await()

    suspend fun getAccounts(): EconomyResult<List<PlayerBalance>> = economy.getAccounts().await()

    suspend fun getTopAccounts(limit: Int, page: Int): EconomyResult<List<PlayerBalance>> = economy.getTopAccounts(limit, page).await()
}

/**
 * Wrap the class with suspend methods, visible for kotlin users
 * @see fi.sulku.hytale.economy.adapters.EconomySuspendAdapter
 * @return EconomySuspend
 */
@JvmSynthetic
fun PlayerEconomy.asCoroutine(): EconomySuspendAdapter = EconomySuspendAdapter(this)