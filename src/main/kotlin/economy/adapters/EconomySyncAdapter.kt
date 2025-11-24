package fi.sulku.hytale.economy.adapters

import fi.sulku.hytale.economy.EconomyResult
import fi.sulku.hytale.economy.PlayerBalance
import fi.sulku.hytale.economy.PlayerEconomy
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Wrapper class for Economy.
 * This can be used if you want to have whole economy in memory like most minecraft servers do.
 */
class EconomySyncAdapter(private val economy: PlayerEconomy) {
    val name: String get() = economy.name
    val currencyPlural: String get() = economy.currencyPlural
    val currencySingular: String get() = economy.currencySingular
    val fractionalDigits: Int get() = economy.fractionalDigits

    fun format(amount: BigDecimal): String = economy.format(amount)

    fun createAccount(uuid: UUID): Boolean = economy.createAccount(uuid).get()

    fun hasAccount(uuid: UUID): Boolean = economy.hasAccount(uuid).get()

    fun getBalance(uuid: UUID): BigDecimal = economy.getBalance(uuid).get()

    fun setBalance(uuid: UUID, amount: BigDecimal): EconomyResult = economy.setBalance(uuid, amount).get()

    fun has(uuid: UUID, amount: BigDecimal): Boolean = economy.has(uuid, amount).get()

    fun withdraw(uuid: UUID, amount: BigDecimal): EconomyResult = economy.withdraw(uuid, amount).get()

    fun deposit(uuid: UUID, amount: BigDecimal): EconomyResult = economy.deposit(uuid, amount).get()

    fun transfer(from: UUID, to: UUID, amount: BigDecimal): EconomyResult = economy.transfer(from, to, amount).get()

    fun getAccounts(): List<PlayerBalance> = economy.getAccounts().get()

    fun getTopAccounts(limit: Int, page: Int): List<PlayerBalance> = economy.getTopAccounts(limit, page).get()

}