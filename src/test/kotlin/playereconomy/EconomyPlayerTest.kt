package playereconomy

import fi.sulku.hytale.economy.adapters.EconomySuspendAdapter
import fi.sulku.hytale.economy.PlayerBalance
import fi.sulku.hytale.economy.PlayerEconomy
import fi.sulku.hytale.economy.ResultType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EconomyPlayerTest {

    private val eco: PlayerEconomy = EconomyPlayer()
    private val economy: EconomySuspendAdapter = eco.asCoroutine()

    @Test
    @DisplayName("new account has correct balance")
    fun createPlayerAccount() {
        runBlocking {
            val uuid = UUID.randomUUID()
            val created = economy.createAccount(uuid)
            economy.deposit(uuid, BigDecimal.ZERO)
            assertTrue(created)

            val hasAccount = economy.hasAccount(uuid)
            assertTrue(hasAccount)

            val balance = economy.getBalance(uuid)
            assertEquals(BigDecimal.ZERO, balance)
        }
    }

    @Test
    @DisplayName("withdraw fails when insufficient funds")
    fun withdraw() = runBlocking {
        val uuid = UUID.randomUUID()
        val initialBalance = BigDecimal.valueOf(20)
        val withdrawAmount = BigDecimal.valueOf(100)
        economy.createAccount(uuid)
        economy.deposit(uuid, initialBalance)

        val result = economy.withdraw(uuid, withdrawAmount)
        assertEquals(result.status, ResultType.FAILURE)

        val balance = economy.getBalance(uuid)
        assertEquals(initialBalance, balance)
    }

    @Test
    @DisplayName("deposit")
    fun deposit() = runBlocking {
        val uuid = UUID.randomUUID()
        val initialBalance = BigDecimal.valueOf(20)
        val depositAmount = BigDecimal.valueOf(100)

        economy.createAccount(uuid)
        economy.deposit(uuid, initialBalance)

        val result = economy.deposit(uuid, depositAmount)
        val expectedValue = initialBalance + depositAmount
        assertEquals(result.status, ResultType.SUCCESS)
        assertEquals(result.balanceBefore, initialBalance)
        assertEquals(result.balanceAfter, expectedValue)

        val balance = economy.getBalance(uuid)
        assertEquals(balance, expectedValue)
    }


    @Test
    @DisplayName("toplist")
    fun checkTopList() = runBlocking {
        repeat(5) {
            val uuid = UUID.randomUUID()
            val initialBalance = it.toBigDecimal().plus(1.toBigDecimal())
            val change = 2.toBigDecimal()
            economy.createAccount(uuid)
            economy.deposit(uuid, initialBalance)
            economy.deposit(uuid, change)
            economy.withdraw(uuid, change)
            val balance = economy.getBalance(uuid)
            assertEquals(balance, initialBalance)
        }

        val accounts = economy.getAccounts()
        assertEquals(accounts.size, 5)

        val topAccounts: List<PlayerBalance> = economy.getTopAccounts(100, 1)

        topAccounts.forEachIndexed { i, playerBalance ->
            val balance = playerBalance.balance
            println("Top ${i + 1}: $balance")
            assertEquals(balance, BigDecimal.valueOf(i.toLong() + 1))
        }
    }
}


