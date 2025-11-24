package playereconomy

import fi.sulku.hytale.economy.EconomyResult
import fi.sulku.hytale.economy.PlayerBalance
import fi.sulku.hytale.economy.PlayerEconomy
import fi.sulku.hytale.economy.ResultType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class EconomyPlayer(override val id: String = "hyconomy") : PlayerEconomy {
    override val isEnabled: Boolean = true
    override val name: String = "Hyconomy"
    override val currencyPlural: String = "€"
    override val currencySingular: String = "€"
    override val fractionalDigits: Int = 2
    override fun format(amount: BigDecimal): String = "$amount$currencySingular"

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private val balances: MutableMap<UUID, BigDecimal> = ConcurrentHashMap()
    }

    override fun hasAccount(uuid: UUID): CompletableFuture<Boolean> = scope.future { balances.containsKey(uuid) }

    override fun getAccounts(): CompletableFuture<List<PlayerBalance>> =
        scope.future { balances.map { PlayerBalance(it.key, it.value) } }

    override fun getTopAccounts(limit: Int, page: Int): CompletableFuture<List<PlayerBalance>> =
        scope.future { balances.map { PlayerBalance(it.key, it.value) }.sortedByDescending { it.balance } }

    override fun getBalance(uuid: UUID): CompletableFuture<BigDecimal> = scope.future { balances[uuid]!! }

    override fun has(uuid: UUID, amount: BigDecimal): CompletableFuture<Boolean> =
        scope.future { getBalance(uuid).await() >= amount }

    override fun createAccount(
        uuid: UUID
    ): CompletableFuture<Boolean> = scope.future { balances.putIfAbsent(uuid, BigDecimal.ZERO) == null }

    override fun withdraw(
        uuid: UUID,
        amount: BigDecimal
    ): CompletableFuture<EconomyResult> = scope.future {
        val hasValidAmount: EconomyResult? = hasPreTransactionError(uuid, amount)
        if (hasValidAmount != null) return@future hasValidAmount

        val before = getBalance(uuid).await()

        if (before <= amount) {
            return@future EconomyResult(
                status = ResultType.FAILURE,
                amount = amount,
                errorMessage = "Player does not have $amount he has $before"
            )
        }

        val after = before - amount
        balances[uuid] = after

        return@future EconomyResult(
            status = ResultType.SUCCESS,
            amount = amount,
            balanceBefore = before,
            balanceAfter = after
        )
    }

    override fun deposit(
        uuid: UUID,
        amount: BigDecimal
    ): CompletableFuture<EconomyResult> = scope.future {
        val hasValidAmount: EconomyResult? = hasPreTransactionError(uuid, amount)
        if (hasValidAmount != null) return@future hasValidAmount

        val before = getBalance(uuid).await()

        // Prevent going over the max amount
        if (before > BigDecimal.valueOf(Long.MAX_VALUE) - amount) {  // todo maxvalue to config?
            return@future EconomyResult(
                status = ResultType.FAILURE,
                errorMessage = "Deposit would exceed maximum balance"
            )
        }

        val after = before + amount
        balances[uuid] = after

        return@future EconomyResult(
            status = ResultType.SUCCESS,
            amount = amount,
            balanceBefore = before,
            balanceAfter = after
        )
    }

    private suspend fun hasPreTransactionError(
        uuid: UUID,
        amount: BigDecimal,
    ): EconomyResult? {
        if (amount < BigDecimal.ZERO) {
            return EconomyResult(
                status = ResultType.FAILURE,
                errorMessage = "Negative amounts are not allowed"
            )
        } else if (!hasAccount(uuid).await()) {
            return EconomyResult(
                status = ResultType.FAILURE,
                errorMessage = "Account does not exist for $uuid"
            )
        }
        return null
    }
}