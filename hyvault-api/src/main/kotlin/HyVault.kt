package fi.sulku.hytale.economy.api

import java.util.concurrent.ConcurrentHashMap

/**
 * Registry implementation to handle economies
 */
object HyVault {

    private val economies = ConcurrentHashMap<String, PlayerEconomy>()

    private var defaultEconomy: PlayerEconomy? = null
    var preferredDefaultId: String? = null

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
     * @return the wrapped economy instance (with all middlewares applied).
     *         Use this returned instance if you need to cache the economy.
     */
    @JvmStatic
    fun registerEconomy(provider: PlayerEconomy): PlayerEconomy  {
        // If multiple economies are disabled, wipe the previous ones
        if (!allowMultiplePlayerEconomies) {
            if (economies.isNotEmpty()) {
                println("[HyVault] Multiple economies disabled. Clearing previous registrations.")
                economies.clear()
                defaultEconomy = null
            }
        }

        if (economies.containsKey(provider.id)) {
            println("[HyVault] Warning: Overwriting existing economy with ID: ${provider.id}")
        }

        val finalProvider = provider
        println("[HyVault-DEBUG] Registering economy '${provider.id}', original type: ${provider::class.java.simpleName}")

        economies[finalProvider.id] = finalProvider

        println("[HyVault] Registered economy provider: '${finalProvider.id}' (${finalProvider.name})")

        refreshDefault()

        return finalProvider
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
            if (specific != null) {
                println("[HyVault-DEBUG] getEconomy('$id') returning instance ${System.identityHashCode(specific)} of type ${specific::class.java.simpleName}")
                return specific
            }
            println("[HyVault] Warning: Requested economy '$id' not found. Falling back to default.")
        }
        // If economy not found with the name take any it can find
        val result = defaultEconomy ?: economies.values.first()
        println("[HyVault-DEBUG] getEconomy(${id ?: "null"}) returning default instance ${System.identityHashCode(result)} of type ${result::class.java.simpleName}")
        return result
    }

    /**
     * Check if any economy is currently loaded.
     */
    @JvmStatic
    fun hasEconomy(): Boolean = economies.isNotEmpty()
}