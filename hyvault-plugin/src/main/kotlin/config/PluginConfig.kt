package fi.sulku.hytale.economy.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PluginConfig(
    @SerialName("allow-multiple-economies")
    var allowMultiple: Boolean = true,

    @SerialName("default-economy-id")
    var defaultEconomyId: String = "hyconomy"
)