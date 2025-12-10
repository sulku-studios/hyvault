package fi.sulku.hytale.economy.api

import java.math.BigDecimal
import java.util.*

/**
 * Economy action result containing transaction information.
 */
sealed class TransactionResult<out T : Action> {
    abstract val economyId: String

    data class Success<T : Action>(
        override val economyId: String,
        val value: T
    ) : TransactionResult<T>()

    data class Failure(
        override val economyId: String,
        val errorMessage: String
    ) : TransactionResult<Nothing>()

    companion object {
        /**
         * Create a successful transaction result.
         *
         * Note: If you're extending AbstractPlayerEconomy, use the `success(action)` helper instead
         * which automatically includes the economyId.
         *
         * @param economyId The economy identifier
         * @param value The action that was executed
         * @return Success result
         */
        @JvmStatic
        fun <T : Action> success(economyId: String, value: T): TransactionResult<T> {
            return Success(economyId, value)
        }

        /**
         * Create a failed transaction result.
         *
         * Note: If you're extending AbstractPlayerEconomy, use the `failure(message)` helper instead
         * which automatically includes the economyId.
         *
         * @param economyId The economy identifier
         * @param errorMessage The error message
         * @return Failure result
         */
        @JvmStatic
        fun failure(economyId: String, errorMessage: String): TransactionResult<Nothing> {
            return Failure(economyId, errorMessage)
        }
    }
}

sealed class Action {
    data class Withdraw(val uuid: UUID, val old: BigDecimal, val new: BigDecimal) : Action()
    data class Deposit(val uuid: UUID, val old: BigDecimal, val new: BigDecimal) : Action()
    data class Set(val uuid: UUID, val old: BigDecimal, val new: BigDecimal) : Action()
    data class Transfer(val from: Withdraw, val to: Deposit) : Action()
}