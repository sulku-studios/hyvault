package fi.sulku.hytale.economy.registry

import fi.sulku.hytale.economy.PlayerEconomy

/**
 * Default registry implementation to handle economies
 *
 * @see EconomyRegistry
 */
class DefaultEconomyRegistry(
    val defaultEconomyId: String,
    val allowMultiplePlayerEconomies: Boolean
) : EconomyRegistry {

    /**
     * Contains all registered economies with <name, economy>
     */
    private val economies = mutableMapOf<String, PlayerEconomy>()

    /**
     * Default economy used
     * If no default economy found it will just use the first one added
     */
    private var defaultEconomy: PlayerEconomy? = null

    /**
     * @param id of the economy
     *
     * @return economy
     */
    override fun getPlayerEconomy(id: String) = economies[id]

    /**
     * @return all registered economies
     */
    override fun getAllPlayerEconomies(): Collection<PlayerEconomy> = economies.values

    /**
     * Gets the default economy
     *
     * @return PlayerEconomy
     */
    override fun getDefaultPlayerEconomy(): PlayerEconomy? = defaultEconomy

    /**
     * Registers economy so it can be found by other plugins
     *
     * @param economy to register
     */
    override fun registerPlayerEconomy(economy: PlayerEconomy) {
        //todo if same name?
        if (!allowMultiplePlayerEconomies && economies.isNotEmpty()) {
            //todo throw error/warning?
            return
        }

        economies[economy.id] = economy
        when {
            defaultEconomy == null -> defaultEconomy = economy // Default to any economy
            economy.name == defaultEconomyId -> defaultEconomy = economy // override with default one
        }
    }
}