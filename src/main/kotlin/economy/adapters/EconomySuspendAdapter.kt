package fi.sulku.hytale.economy.adapters

import fi.sulku.hytale.economy.EconomyResult
import fi.sulku.hytale.economy.PlayerBalance
import fi.sulku.hytale.economy.PlayerEconomy
import kotlinx.coroutines.future.await
import java.math.BigDecimal
import java.util.UUID

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

    suspend fun createAccount(uuid: UUID): Boolean = economy.createAccount(uuid).await()

    suspend fun hasAccount(uuid: UUID): Boolean = economy.hasAccount(uuid).await()

    suspend fun getBalance(uuid: UUID): BigDecimal = economy.getBalance(uuid).await()

    suspend fun has(uuid: UUID, amount: BigDecimal): Boolean = economy.has(uuid, amount).await()

    suspend fun withdraw(uuid: UUID, amount: BigDecimal): EconomyResult = economy.withdraw(uuid, amount).await()

    suspend fun deposit(uuid: UUID, amount: BigDecimal): EconomyResult = economy.deposit(uuid, amount).await()

    suspend fun getAccounts(): List<PlayerBalance> = economy.getAccounts().await()

    suspend fun getTopAccounts(limit: Int, page: Int): List<PlayerBalance> = economy.getTopAccounts(limit, page).await()
}