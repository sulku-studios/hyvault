package fi.sulku.hytale.economy.api.events

import fi.sulku.hytale.economy.api.Action
import fi.sulku.hytale.economy.api.TransactionResult
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer
import kotlin.jvm.java

/**
 * Event bus for economy events.
 *
 *
 * Kotlin:
 * EconomyEventBus.subscribe<Action.Set> { result ->
 *     when (result) {
 *         is TransactionResult.Success -> println("Set balance: ${result.value.new}")
 *         is TransactionResult.Failure -> println("Failed: ${result.errorMessage}")
 *     }
 * }
 *
 * Java:
 * EconomyEventBus.subscribe(Action.Set.class, result -> {
 *     if (result instanceof TransactionResult.Success) {
 *         Action.Set action = ((TransactionResult.Success<?>) result).getValue();
 *         System.out.println("Set balance: " + action.getNew());
 *     }
 * });
 */
object EconomyEvents {

    private val listeners = ConcurrentHashMap<Class<out Action>, CopyOnWriteArrayList<Consumer<TransactionResult<Action>>>>()

    /**
     * Register a listener for a specific action type (Java-friendly version)
     *
     * @param actionType The class of the action to listen for
     * @param listener The callback to invoke when the action is published
     */
    @JvmStatic
    fun <T : Action> subscribe(actionType: Class<T>, listener: Consumer<TransactionResult<T>>) {
        val listenerList = listeners.computeIfAbsent(actionType) { CopyOnWriteArrayList() }
        @Suppress("UNCHECKED_CAST")
        listenerList.add(listener as Consumer<TransactionResult<Action>>)
    }

    /**
     * Register a listener for a specific action type (Kotlin-friendly with reified type)
     *
     * Usage: `EconomyEventBus.subscribe<Action.Set> { result -> ... }`
     */
    inline fun <reified T : Action> subscribe(noinline listener: (TransactionResult<T>) -> Unit) {
        subscribe(T::class.java, Consumer(listener))
    }

    /**
     * Publish a transaction result to all registered listeners
     *
     * @param result The transaction result to publish
     */
    @JvmStatic
    fun <T : Action> publish(result: TransactionResult<T>?) {
        result ?: return

        // Only publish Success results with actions, skip Failure results
        val action = when (result) {
            is TransactionResult.Success -> result.value
            is TransactionResult.Failure -> return
        }

        listeners[action::class.java]?.forEach { listener ->
            runCatching {
                @Suppress("UNCHECKED_CAST")
                listener.accept(result as TransactionResult<Action>)
            }.onFailure { e ->
                System.err.println("[HyVault] Error in event listener: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Unregister a listener (Java-friendly version)
     */
    @JvmStatic
    fun <T : Action> unsubscribe(actionType: Class<T>, listener: Consumer<TransactionResult<T>>) {
        @Suppress("UNCHECKED_CAST")
        listeners[actionType]?.remove(listener as Consumer<TransactionResult<Action>>)
    }

    /**
     * Kotlin-friendly unsubscribe with reified type
     */
    inline fun <reified T : Action> unsubscribe(noinline listener: (TransactionResult<T>) -> Unit) {
        unsubscribe(T::class.java, Consumer(listener))
    }

    /**
     * Clear all listeners (useful for plugin reload)
     */
    @JvmStatic
    fun clearAll() {
        listeners.clear()
    }
}
