import fi.sulku.hytale.economy.HyVaultPlugin
import fi.sulku.hytale.economy.api.HyVault
import org.junit.jupiter.api.Test
import economy.MockEconomy

class HyVaultPluginTest {
    @Test
    fun onStart() {
        val hyvault = HyVaultPlugin()
        hyvault.onEnable()
        HyVault.registerEconomy(MockEconomy("testeco"))
        val eco = HyVault.getEconomy()
        println("Default $eco")
    }
}