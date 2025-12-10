package fi.sulku.hytale.economy.messaging

import fi.sulku.hytale.economy.messaging.payload.SerializedTransactionResult

/**
 * Abstraction over cross-server messaging so transports (Redis, etc.) can be swapped without
 * affecting hyvault-api or third-party plugins.
 */
interface MessagingBridge {
    /** Start listening for inbound events. */
    fun start(onInbound: (SerializedTransactionResult) -> Unit)

    /** Publish a serialized economy event. */
    fun publish(event: SerializedTransactionResult)

    /** Release any external resources. */
    fun stop()
}
