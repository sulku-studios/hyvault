package fi.sulku.hytale.economy.api

import java.util.concurrent.ConcurrentHashMap

/**
 * Registry implementation to handle economies
 */
object HyVault {

    private val economies = ConcurrentHashMap<String, PlayerEconomy>()
    private var defaultEconomy: PlayerEconomy? = null
    private var preferredDefaultId: String? = null

    @JvmField
    var allowMultiplePlayerEconomies: Boolean = true


    /**
     * Called by hyvault-plugin onEnable to set the preferred ID from config.
     * @param id The ID string from config.yml (e.g. "gold")
     */
    @JvmStatic
    fun setConfiguredDefault(id: String) {
        preferredDefaultId = id
        refreshDefault()
    }

    // For providers

    /**
     * Register a new economy provider.
     *
     * @param provider to register.
     */
    @JvmStatic
    fun registerEconomy(provider: PlayerEconomy) {
        // If multiple economies are disabled, wipe the previous ones
        if (!allowMultiplePlayerEconomies) {
            if (economies.isNotEmpty()) {
                println("[HyVault] Multiple economies disabled. Clearing previous registrations.")
                economies.clear()
                defaultEconomy = null
            }
        }

        // Warn if overwriting
        if (economies.containsKey(provider.id)) {
            println("[HyVault] Warning: Overwriting existing economy with ID: ${provider.id}")
        }

        economies[provider.id] = provider
        println("[HyVault] Registered economy provider: '${provider.id}' (${provider.name})")

        // Recalculate new default
        refreshDefault()
    }

    /**
     * Unregister an economy (e.g. on plugin disable).
     */
    @JvmStatic
    fun unregisterEconomy(id: String) {
        economies.remove(id)
        // Sets new default if default economy is removed
        if (defaultEconomy?.id == id) {
            defaultEconomy = null
            refreshDefault()
        }
    }

    /**
     * Refreshes when other economy is loaded
     * This ensures that if economy with default name is loaded after other economy its set to default.
     */
    private fun refreshDefault() {
        // 1. Priority: If we found the one the config wants, strictly use it.
        val preferred = economies[preferredDefaultId]
        if (preferred != null) {
            if (defaultEconomy?.id != preferred.id) {
                println("[HyVault] Default economy set to preferred: ${preferred.id}")
            }
            defaultEconomy = preferred
            return
        }

        if (defaultEconomy == null && economies.isNotEmpty()) {
            val fallback = economies.values.first()
            defaultEconomy = fallback
            println("[HyVault] Default economy auto-selected: ${fallback.id}")
        }
    }

    // For shop plugins
    /**
     * Returns all registered economies
     */
    @JvmStatic
    fun getAllEconomies(): MutableCollection<PlayerEconomy> = economies.values

    /**
     * Get default economy or economy by name
     * @param id of the economy
     *
     * @return PlayerEconomy
     */
    @JvmStatic
    @JvmOverloads
    fun getEconomy(id: String? = null): PlayerEconomy {
        if (economies.isEmpty()) {
            throw IllegalStateException("HyVault: No Economy plugin found! Please install one.")
        }

        if (id != null) {
            val specific = economies[id]
            if (specific != null) return specific
            println("[HyVault] Warning: Requested economy '$id' not found. Falling back to default.")
        }
        // If economy not found with the name take any it can find
        return defaultEconomy ?: economies.values.first()
    }

    /**
     * Check if any economy is currently loaded.
     */
    @JvmStatic
    fun hasEconomy(): Boolean = economies.isNotEmpty()
}