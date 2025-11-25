package fi.sulku.hytale.economy.api.adapters

import fi.sulku.hytale.economy.api.PlayerBalance
import fi.sulku.hytale.economy.api.PlayerEconomy
import fi.sulku.hytale.economy.api.BalanceChange
import fi.sulku.hytale.economy.api.EconomyResult
import fi.sulku.hytale.economy.api.Transfer
import java.math.BigDecimal
import java.util.*

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

    fun createAccount(uuid: UUID): EconomyResult<Boolean>? = economy.createAccount(uuid).get()

    fun hasAccount(uuid: UUID): EconomyResult<Boolean>? = economy.hasAccount(uuid).get()

    fun getBalance(uuid: UUID): EconomyResult<BigDecimal>? = economy.getBalance(uuid).get()

    fun setBalance(uuid: UUID, amount: BigDecimal): EconomyResult<BalanceChange> = economy.setBalance(uuid, amount).get()

    fun withdraw(uuid: UUID, amount: BigDecimal): EconomyResult<BalanceChange> = economy.withdraw(uuid, amount).get()

    fun deposit(uuid: UUID, amount: BigDecimal): EconomyResult<BalanceChange> = economy.deposit(uuid, amount).get()

    fun transfer(from: UUID, to: UUID, amount: BigDecimal): EconomyResult<Transfer> = economy.transfer(from, to, amount).get()

    fun getAccounts(): EconomyResult<List<PlayerBalance>> = economy.getAccounts().get()

    fun getTopAccounts(limit: Int, page: Int): EconomyResult<List<PlayerBalance>> = economy.getTopAccounts(limit, page).get()
}