package fi.sulku.hytale.economy.messaging

import fi.sulku.hytale.economy.messaging.payload.SerializedTransactionResult
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.pubsub.RedisPubSubListener
import kotlinx.serialization.json.Json

/**
 * Redis pub/sub implementation for broadcasting economy events across servers.
 */
class RedisMessagingBridge(
    redisUrl: String,
    private val topic: String = "hyvault-events",
    private val json: Json = defaultJson
) : MessagingBridge {

    private val client: RedisClient = RedisClient.create(redisUrl)
    private var pubConnection: StatefulRedisConnection<String, String>? = null
    private var subConnection: StatefulRedisPubSubConnection<String, String>? = null

    override fun start(onInbound: (SerializedTransactionResult) -> Unit) {
        pubConnection = client.connect()
        subConnection = client.connectPubSub()

        val listener = object : RedisPubSubListener<String, String> {
            override fun message(channel: String?, message: String?) {
                if (channel == topic && !message.isNullOrBlank()) {
                    runCatching {
                        onInbound(json.decodeFromString(SerializedTransactionResult.serializer(), message))
                    }.onFailure {
                        println("[HyVault] Failed to decode inbound cross-server event: ${it.message}")
                        it.printStackTrace()
                    }
                }
            }
            override fun message(pattern: String?, channel: String?, message: String?) {}
            override fun subscribed(channel: String?, count: Long) {}
            override fun psubscribed(pattern: String?, count: Long) {}
            override fun unsubscribed(channel: String?, count: Long) {}
            override fun punsubscribed(pattern: String?, count: Long) {}
        }

        subConnection?.addListener(listener)
        subConnection?.sync()?.subscribe(topic)
    }

    override fun publish(event: SerializedTransactionResult) {
        val payload = runCatching {
            json.encodeToString(SerializedTransactionResult.serializer(), event)
        }.getOrElse {
            println("[HyVault] Failed to serialize cross-server event: ${it.message}")
            return
        }

        val publishConnection: RedisCommands<String, String>? = pubConnection?.sync()
        if (publishConnection == null) {
            println("[HyVault] Redis publish skipped: no connection")
            return
        }

        runCatching { publishConnection.publish(topic, payload) }
            .onFailure {
                println("[HyVault] Failed to publish cross-server event: ${it.message}")
                it.printStackTrace()
            }
    }

    override fun stop() {
        runCatching { subConnection?.close() }
        runCatching { pubConnection?.close() }
        runCatching { client.shutdown() }
    }

    companion object {
        val defaultJson: Json = Json { ignoreUnknownKeys = true }
    }
}
