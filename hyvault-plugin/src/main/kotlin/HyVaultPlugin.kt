package fi.sulku.hytale.economy

import fi.sulku.hytale.economy.api.Action
import fi.sulku.hytale.economy.api.HyVault
import fi.sulku.hytale.economy.api.TransactionResult
import fi.sulku.hytale.economy.api.events.EconomyEvents
import fi.sulku.hytale.economy.config.ConfigHolder
import fi.sulku.hytale.economy.messaging.MessagingBridge
import fi.sulku.hytale.economy.messaging.RedisMessagingBridge
import fi.sulku.hytale.economy.messaging.payload.toSerialized
import fi.sulku.hytale.economy.messaging.payload.toTransactionResult
import fi.sulku.hytale.economy.web.WebServer
import org.bukkit.plugin.java.JavaPlugin


class HyVaultPlugin : JavaPlugin() {
    //todo Economy transfer, switch dep to compile then

    private lateinit var configHolder: ConfigHolder
    private var webServer: WebServer? = null
    private var messaging: MessagingBridge? = null
    private val suppressOutbound = ThreadLocal.withInitial { false }


    override fun onEnable() {
        println("TEXTXX" + HyVault.preferredDefaultId)
        HyVault.preferredDefaultId = "Test"
        println("X ${HyVault.preferredDefaultId}")

        //todo send to crossplatform?

       // webServer = WebServer(8080, logger)
      //  webServer?.start()

        setupCrossServerMessaging()


        // Mirror all actions outbound to messaging (if enabled)
        EconomyEvents.subscribe<Action.Withdraw> { publishCrossServer(it) }
        EconomyEvents.subscribe<Action.Deposit> { publishCrossServer(it) }
        EconomyEvents.subscribe<Action.Set> { publishCrossServer(it) }
        EconomyEvents.subscribe<Action.Transfer> { publishCrossServer(it) }


        /*
                val dataFolder = File("plugins/hyvault")
                val configFile = File(dataFolder, "config.yml")

                configHolder = ConfigHolder(configFile)
                val config = configHolder.config

                HyVault.allowMultiplePlayerEconomies = config.allowMultiple

                if (config.defaultEconomyId.isNotEmpty()) {
                    HyVault.setConfiguredDefault(config.defaultEconomyId)
                }

                val registrations = this.server.servicesManager
                        .getRegistrations(PlayerEconomy::class.java)

                for (rsp in registrations) {
                    val provider: PlayerEconomy = rsp.getProvider()



                    // You can identify them by the plugin that provided them
                    val pluginName: String = rsp.plugin.name

                    logger.info("Found economy provided by: $pluginName")


                    // Store it in your internal map/list
                    // myEconomyMap.put(pluginName, provider);
                }*/
    }

    override fun onDisable() {
        println("HyVault unloading. Clearing registry.")
        HyVault.getAllEconomies().clear()
        webServer?.stop()
        messaging?.stop()
        messaging = null
    }

    private fun setupCrossServerMessaging() {
        val redisUrl =
            "rediss://default:xx.upstash.io:6379"?.ifBlank { null }
                ?: return
        val topicBase = System.getenv("HYVAULT_REDIS_TOPIC")?.ifBlank { null } ?: "hyvault-events"
        val prefix = System.getenv("HYVAULT_REDIS_PREFIX")?.trim().orEmpty()
        val topic = if (prefix.isNotEmpty()) "$prefix:$topicBase" else topicBase

        messaging = RedisMessagingBridge(redisUrl, topic).also { bridge ->
            bridge.start { inbound ->
                suppressOutbound.set(true)
                try {
                    EconomyEvents.publish(inbound.toTransactionResult())
                } finally {
                    suppressOutbound.set(false)
                }
            }
            logger.info("HyVault cross-server messaging enabled via Redis topic '$topic'")
        }
    }

    private fun publishCrossServer(result: TransactionResult<Action>) {
        val bridge = messaging ?: return
        if (suppressOutbound.get()) return

        result.toSerialized()?.let {
            bridge.publish(it)
        }
    }
}

