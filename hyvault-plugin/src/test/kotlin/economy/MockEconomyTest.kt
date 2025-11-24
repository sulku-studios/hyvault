package economy

import fi.sulku.hytale.economy.api.PlayerBalance
import fi.sulku.hytale.economy.api.PlayerEconomy
import fi.sulku.hytale.economy.api.ResultType
import fi.sulku.hytale.economy.api.adapters.EconomySuspendAdapter
import fi.sulku.hytale.economy.api.adapters.asCoroutine
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockEconomyTest {
    private val eco: PlayerEconomy = MockEconomy()
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
        val totalAccounts = 5

        repeat(totalAccounts) { i ->
            val uuid = UUID.randomUUID()
            val initialBalance = BigDecimal(i + 1)
            economy.createAccount(uuid)
            economy.deposit(uuid, initialBalance)
        }

        val topAccounts: List<PlayerBalance> = economy.getTopAccounts(100, 1)

        assertEquals(totalAccounts, topAccounts.size)

        topAccounts.forEachIndexed { i, playerBalance ->
            val balance = playerBalance.balance
            val expectedBalance = BigDecimal(totalAccounts - i)
            assertEquals(expectedBalance, balance)
        }
    }
}




