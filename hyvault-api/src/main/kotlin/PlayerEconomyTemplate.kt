package fi.sulku.hytale.economy.api

import fi.sulku.hytale.economy.api.events.EconomyEvents
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Base implementation of PlayerEconomy that automatically fires events.
 * Economy implementations can extend this to get event publishing.
 *
 * This allows metrics, logging, and other plugins to listen to economy operations
 * without needing wrapper implementations.
 */
abstract class PlayerEconomyTemplate : PlayerEconomy {
//todo plugin? also pass that for logs or hadnel on register and connect with plugin there?
    /**
     * Implement this to perform the actual withdraw operation
     */
    protected abstract fun withdrawImpl(uuid: UUID, amount: BigDecimal): CompletableFuture<TransactionResult<Action.Withdraw>>

    /**
     * Implement this to perform the actual deposit operation
     */
    protected abstract fun depositImpl(uuid: UUID, amount: BigDecimal): CompletableFuture<TransactionResult<Action.Deposit>>

    /**
     * Implement this to perform the actual transfer operation
     */
    protected abstract fun transferImpl(
        from: UUID,
        to: UUID,
        amount: BigDecimal
    ): CompletableFuture<TransactionResult<Action.Transfer>>

    /**
     * Implement this to perform the actual set balance operation
     */
    protected abstract fun setBalanceImpl(uuid: UUID, amount: BigDecimal): CompletableFuture<TransactionResult<Action.Set>>

    override fun withdraw(uuid: UUID, amount: BigDecimal): CompletableFuture<TransactionResult<Action.Withdraw>> {
        return publishOnSuccess(withdrawImpl(uuid, amount))
    }

    override fun deposit(uuid: UUID, amount: BigDecimal): CompletableFuture<TransactionResult<Action.Deposit>> {
        return publishOnSuccess(depositImpl(uuid, amount))
    }

    override fun transfer(from: UUID, to: UUID, amount: BigDecimal): CompletableFuture<TransactionResult<Action.Transfer>> {
        return publishOnSuccess(transferImpl(from, to, amount))
    }

    override fun setBalance(uuid: UUID, amount: BigDecimal): CompletableFuture<TransactionResult<Action.Set>> {
        return publishOnSuccess(setBalanceImpl(uuid, amount))
    }

    private fun <T : Action> publishOnSuccess(future: CompletableFuture<TransactionResult<T>>): CompletableFuture<TransactionResult<T>> {
        future.whenComplete { result, error ->
            if (error == null && result != null) {
                EconomyEvents.publish(result)
            }
        }
        return future
    }

    /**
     * Helper method to create a successful transaction result with automatic economyId
     *
     * @param value The action that was executed
     * @return Success result with this economy's ID
     */
    protected fun <T : Action> success(value: T): TransactionResult<T> {
        return TransactionResult.Success(id, value)
    }

    /**
     * Helper method to create a failed transaction result with automatic economyId
     *
     * @param errorMessage The error message
     * @return Failure result with this economy's ID
     */
    protected fun failure(errorMessage: String): TransactionResult<Nothing> {
        return TransactionResult.Failure(id, errorMessage)
    }
}

