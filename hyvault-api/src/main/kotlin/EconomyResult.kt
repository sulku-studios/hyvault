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
data class EconomyResult(
    val status: ResultType,
    val amount: BigDecimal? = null,
    val balanceBefore: BigDecimal? = null,
    val balanceAfter: BigDecimal? = null,
    val errorMessage: String = "",
)

/**
 * Result type of the economy action
 */
enum class ResultType {
    SUCCESS,
    FAILURE
}