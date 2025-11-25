package fi.sulku.hytale.economy.api

import fi.sulku.hytale.economy.api.adapters.EconomySyncAdapter
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture

interface PlayerEconomy {

    /**
     * Checks if the economy is enabled.
     * @return true if enabled
     */
    val isEnabled: Boolean

    /**
     * Identifier for economy example: "myeconomy".
     */
    val id: String

    /**
     * Display name for economy example: "MyEconomy.
     */
    val name: String

    //Todo
    /**
     * Icon for the currency.
     */
    // <stacksize, image path>
    // val stackIcons: Map<Int, String>

    /**
     * Returns the name of the currency plural.
     *
     * @return name of the currency plural.
     */
    val currencyPlural: String

    /**
     * Returns the name of the currency singular.
     *
     * @return name of the currency singular.
     */
    val currencySingular: String

    /**
     * Decides how many decimal points will be in the currency.
     * Example:
     * User has currency amount of 234
     * fractionalDigits = 2
     * Then the currency will be 2.34
     *
     * @amount to format
     * @return human readable string describing the amount
     */
    val fractionalDigits: Int

    /**
     * Format amount into readable form.
     *
     * @amount to format
     */
    fun format(amount: BigDecimal): String

    /**
     * Tries to create account for the player.
     *
     * @param uuid of the player
     * @return future with true/false
     */
    fun createAccount(uuid: UUID): CompletableFuture<EconomyResult<Boolean>>

    /**
     * Checks if the user has account on the server yet.
     * Should return true if player has been on the server.
     *
     * @param uuid of the player
     *@return future with true/false
     */
    fun hasAccount(uuid: UUID): CompletableFuture<EconomyResult<Boolean>>

    /**
     * Gets the balance of the player.
     *
     *  @param uuid of the player
     *
     *  @return future for current balance
     */
    fun getBalance(uuid: UUID): CompletableFuture<EconomyResult<BigDecimal>>

    /**
     * Sets the user balance.
     *
     * @param uuid of the player
     *
     * @return future for economy result
     */
    fun setBalance(uuid: UUID, amount: BigDecimal): CompletableFuture<EconomyResult<BalanceChange>>

    /**
     * @param amount to deposit
     * @param uuid of the player
     *
     * @return future for economy result
     */
    fun deposit(uuid: UUID, amount: BigDecimal): CompletableFuture<EconomyResult<BalanceChange>>

    /**
     * Withdraw amount from the player.
     *
     * @param amount to withdraw
     * @param uuid of the player
     *
     * @return future for economy result
     */
    fun withdraw(uuid: UUID, amount: BigDecimal): CompletableFuture<EconomyResult<BalanceChange>>

    /**
     * Transfer amount from 1 player to another.
     *
     * @param from user
     * @param to user
     * @param amount to transfer
     *
     * @return future for economy result
     */
    fun transfer(from: UUID, to: UUID, amount: BigDecimal): CompletableFuture<EconomyResult<Transfer>>

    /**
     *
     *
     * @return future with List<PlayerBalance> containing all balances
     */
    fun getAccounts(): CompletableFuture<EconomyResult<List<PlayerBalance>>>

    /**
     *
     * @param limit per page
     * @param page which page you are on, example: if limit is 5 and u go to page 2 it will show 5-10
     *
     * @return future of the top account balances
     */
    fun getTopAccounts(limit: Int, page: Int): CompletableFuture<EconomyResult<List<PlayerBalance>>>

    /**
     * Wrap the class with synchronized calls, minecraft style
     * @see fi.sulku.hytale.economy.adapters.EconomySyncAdapter
     * @return EconomySync
     */
    fun asSync(): EconomySyncAdapter = EconomySyncAdapter(this)
}

/**
 * Helper object for listing balances used in getTopAccounts and getAccounts
 */
data class PlayerBalance(val uuid: UUID, val balance: BigDecimal)