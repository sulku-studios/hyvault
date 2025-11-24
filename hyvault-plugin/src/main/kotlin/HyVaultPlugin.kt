package fi.sulku.hytale.economy

import fi.sulku.hytale.economy.api.HyVault
import fi.sulku.hytale.economy.config.ConfigHolder
import java.io.File

class HyVaultPlugin {

    private lateinit var configHolder: ConfigHolder

    fun onEnable() {
        println("HyVault Core is enabling...")

        val dataFolder = File("plugins/hyvault")
        val configFile = File(dataFolder, "config.yml")

        configHolder = ConfigHolder(configFile)
        val config = configHolder.config

        HyVault.allowMultiplePlayerEconomies = config.allowMultiple

        if (config.defaultEconomyId.isNotEmpty()) {
            HyVault.setConfiguredDefault(config.defaultEconomyId)
        }
    }

    fun onDisable() {
        println("HyVault unloading. Clearing registry.")
        HyVault.getAllEconomies().clear()
    }
}

