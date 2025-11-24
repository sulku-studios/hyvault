package fi.sulku.hytale.economy.config

import com.charleskorn.kaml.Yaml
import java.io.File

class ConfigHolder(private val configFile: File) {

    var config: PluginConfig

    init {
        config = load()
    }

    private fun load(): PluginConfig {
        configFile.parentFile?.mkdirs()

        if (!configFile.exists()) {
            save(PluginConfig())
        }

        val yamlString = configFile.readText()
        return Yaml.default.decodeFromString(PluginConfig.serializer(), yamlString)
    }

    fun save(newConfig: PluginConfig = this.config) {
        val updatedYamlString = Yaml.default.encodeToString(PluginConfig.serializer(), newConfig)
        configFile.writeText(updatedYamlString)
    }
}