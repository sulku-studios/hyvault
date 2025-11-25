package fi.sulku.hytale.economy.api

import java.math.BigDecimal

/**
 * Economy action result containing some extra info
 *
 * @param status of the transaction
 * @param amount associated with that transaction
 * @param balanceBefore the transaction
 * @param balanceAfter the transaction
 * @param errorMessage of the failed transaction
 */
data class EconomyResult<T>(
    val success: Boolean,
    val errorMessage: String = "",
    val value: T? = null
)

data class BalanceChange(
    val old: BigDecimal,
    val new: BigDecimal
)

data class Transfer(
    val from: BalanceChange,
    val to: BalanceChange
)