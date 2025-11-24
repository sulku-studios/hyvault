package fi.sulku.hytale

import fi.sulku.hytale.economy.registry.DefaultEconomyRegistry

object HyVault {
    /**
     * Registry containing all the economies
     */
    @JvmStatic
    lateinit var economyRegistry: DefaultEconomyRegistry

    /**
     * Method called onLoad to create registry
     * @return DefaultEconomyRegistry where you register economies
     */
    @JvmStatic
    fun init(
        defaultEconomyId: String,
        allowMultiplePlayerEconomies: Boolean
    ): DefaultEconomyRegistry {
        economyRegistry = DefaultEconomyRegistry(
            defaultEconomyId,
            allowMultiplePlayerEconomies
        )
        return economyRegistry
    }
}