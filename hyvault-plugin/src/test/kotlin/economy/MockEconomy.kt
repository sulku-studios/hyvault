package economy

import fi.sulku.hytale.economy.api.EconomyResult
import fi.sulku.hytale.economy.api.PlayerBalance
import fi.sulku.hytale.economy.api.PlayerEconomy
import fi.sulku.hytale.economy.api.ResultType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class MockEconomy(override val id: String = "hyconomy") : PlayerEconomy {
    override val isEnabled: Boolean = true
    override val name: String = "Hyconomy"
    override val currencyPlural: String = "€"
    override val currencySingular: String = "€"
    override val fractionalDigits: Int = 2
    override fun format(amount: BigDecimal): String = "$amount$currencySingular"

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val balances: MutableMap<UUID, BigDecimal> = ConcurrentHashMap()

    override fun hasAccount(uuid: UUID): CompletableFuture<Boolean> = scope.future { balances.containsKey(uuid) }

    override fun getAccounts(): CompletableFuture<List<PlayerBalance>> =
        scope.future { balances.map { PlayerBalance(it.key, it.value) } }

    override fun getTopAccounts(limit: Int, page: Int): CompletableFuture<List<PlayerBalance>> =
        scope.future { balances.map { PlayerBalance(it.key, it.value) }.sortedByDescending { it.balance } }

    override fun getBalance(uuid: UUID): CompletableFuture<BigDecimal> = scope.future { balances[uuid]!! }

    override fun setBalance(uuid: UUID, amount: BigDecimal): CompletableFuture<EconomyResult> {
        return transaction(
            uuid,
            amount,
            validator = { _ ->
                if (amount > BigDecimal.valueOf(Long.MAX_VALUE) - amount) {
                    "Amount would exceed maximum balance"
                } else {
                    null
                }
            },
            calculator = { _ -> amount }
        )
    }

    override fun has(uuid: UUID, amount: BigDecimal): CompletableFuture<Boolean> =
        scope.future { getBalance(uuid).await() >= amount }

    override fun createAccount(
        uuid: UUID
    ): CompletableFuture<Boolean> = scope.future { balances.putIfAbsent(uuid, BigDecimal.ZERO) == null }

    override fun withdraw(
        uuid: UUID,
        amount: BigDecimal
    ): CompletableFuture<EconomyResult> {
        return transaction(
            uuid,
            amount,
            validator = { current ->
                if (current < amount) {
                    "Insufficient funds: Has $current, needs $amount"
                } else {
                    null
                }
            },
            calculator = { current -> current - amount }
        )
    }

    override fun deposit(
        uuid: UUID,
        amount: BigDecimal
    ): CompletableFuture<EconomyResult> {
        return transaction(
            uuid,
            amount,
            validator = { current ->
                if (current > BigDecimal.valueOf(Long.MAX_VALUE) - amount) {
                    "Deposit would exceed maximum balance"
                } else {
                    null
                }
            },
            calculator = { current -> current + amount }
        )
    }

    override fun transfer(
        from: UUID,
        to: UUID,
        amount: BigDecimal
    ): CompletableFuture<EconomyResult> = scope.future {
        return@future EconomyResult(ResultType.FAILURE, errorMessage = "Not Implemented")
    }

    private fun transaction(
        uuid: UUID,
        amount: BigDecimal,
        validator: (BigDecimal) -> String?,
        calculator: (BigDecimal) -> BigDecimal
    ): CompletableFuture<EconomyResult> = scope.future {
        var result: EconomyResult? = null
        if (amount < BigDecimal.ZERO) {
            return@future EconomyResult(status = ResultType.FAILURE, errorMessage = "Negative amounts are not allowed")
        }

        balances.compute(uuid) { _, currentBalance ->
            if (currentBalance == null) {
                result = EconomyResult(
                    status = ResultType.FAILURE,
                    errorMessage = "Account does not exist for $uuid"
                )
                return@compute null
            }

            val errorMsg = validator(currentBalance)

            if (errorMsg != null) {
                result = EconomyResult(
                    status = ResultType.FAILURE,
                    errorMessage = errorMsg
                )
                return@compute currentBalance
            }
            val newBalance = calculator(currentBalance)

            result = EconomyResult(
                status = ResultType.SUCCESS,
                amount = amount,
                balanceBefore = currentBalance,
                balanceAfter = newBalance
            )
            return@compute newBalance
        }
        return@future result!!
    }
}