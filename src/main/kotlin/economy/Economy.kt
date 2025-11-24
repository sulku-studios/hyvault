package fi.sulku.hytale.economy

import fi.sulku.hytale.economy.adapters.EconomySuspendAdapter
import fi.sulku.hytale.economy.adapters.EconomySyncAdapter
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
     * Icon for the currency
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
     * Decides how many decimal points will be in the currency
     * since the plugin uses longs instead of doubles to have accurate currency rounding
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
     * Tries to create account for the player
     *
     * @param uuid of the player
     * @return future with true/false
     */
    fun createAccount(uuid: UUID): CompletableFuture<Boolean>
    //todo json economy?

    /**
     * Checks if the user has account on the server yet
     * Should return true if player has been on the server
     *
     * @param uuid of the player
     *@return future with true/false
     */
    fun hasAccount(uuid: UUID): CompletableFuture<Boolean>

    /**
     * Gets the balance of the player
     *
     *  @param uuid of the player
     *
     *  @return future for current balance
     */
    fun getBalance(uuid: UUID): CompletableFuture<BigDecimal>

    /**
     * @param amount to deposit
     * @param uuid of the player
     *
     * @return future for economy result
     */
    fun deposit(uuid: UUID, amount: BigDecimal): CompletableFuture<EconomyResult>

    /**
     * @param amount to withdraw
     * @param uuid of the player
     *
     * @return future for economy result
     */
    fun withdraw(uuid: UUID, amount: BigDecimal): CompletableFuture<EconomyResult>

    /**
     *
     * @param uuid of the player
     *
     * @return future with true/false
     */
    fun has(uuid: UUID, amount: BigDecimal): CompletableFuture<Boolean> = getBalance(uuid).thenApply { it >= amount }

    /**
     *
     *
     * @return future with List<PlayerBalance> containing all balances
     */
    fun getAccounts(): CompletableFuture<List<PlayerBalance>>

    /**
     *
     * @param limit per page
     * @param page which page you are on, example: if limit is 5 and u go to page 2 it will show 5-10
     *
     * @return future of the top account balances
     */
    fun getTopAccounts(limit: Int, page: Int): CompletableFuture<List<PlayerBalance>>

    /**
     * Wrap the class with synchronized calls, minecraft style
     * @see fi.sulku.hytale.economy.adapters.EconomySyncAdapter
     * @return EconomySync
     */
    fun asSync(): EconomySyncAdapter = EconomySyncAdapter(this)

    // Support for kotlin wrapper to have suspend functions
    /**
     * Wrap the class with suspend methods, visible for kotlin users
     * @see fi.sulku.hytale.economy.adapters.EconomySuspendAdapter
     * @return EconomySuspend
     */
    @JvmSynthetic
    fun asCoroutine(): EconomySuspendAdapter = EconomySuspendAdapter(this)
}

/**
 * Helper object for listing balances used in getTopAccounts and getAccounts
 */
data class PlayerBalance(val uuid: UUID, val balance: BigDecimal) // todo remove?