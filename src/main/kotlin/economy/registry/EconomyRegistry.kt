package fi.sulku.hytale.economy.registry

import fi.sulku.hytale.economy.PlayerEconomy

/**
 * Registry to handle multiple economies
 */
// Todo have to see if hytale has a way to register services, if has delete this
sealed interface EconomyRegistry {
    /**
     * Registers economy so it can be found by other plugins
     *
     * @param economy to register
     */
    fun registerPlayerEconomy(economy: PlayerEconomy)

    /**
     * @param id of the economy
     *
     * @return economy
     */
    fun getPlayerEconomy(id: String): PlayerEconomy?

    /**
     * @return all registered economies
     */
    fun getAllPlayerEconomies(): Collection<PlayerEconomy>

    /**
     * Gets the default economy
     *
     * @return PlayerEconomy
     */
    fun getDefaultPlayerEconomy(): PlayerEconomy?
}