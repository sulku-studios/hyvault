package fi.sulku.hytale.economy.messaging.payload

import fi.sulku.hytale.economy.api.Action
import fi.sulku.hytale.economy.api.TransactionResult
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal
import java.util.*

@Serializable
sealed class SerializedAction {
    @Serializable
    data class Withdraw(
        val uuid: String,
        @Serializable(with = BigDecimalAsStringSerializer::class) val old: BigDecimal,
        @Serializable(with = BigDecimalAsStringSerializer::class) val new: BigDecimal
    ) : SerializedAction()

    @Serializable
    data class Deposit(
        val uuid: String,
        @Serializable(with = BigDecimalAsStringSerializer::class) val old: BigDecimal,
        @Serializable(with = BigDecimalAsStringSerializer::class) val new: BigDecimal
    ) : SerializedAction()

    @Serializable
    data class Set(
        val uuid: String,
        @Serializable(with = BigDecimalAsStringSerializer::class) val old: BigDecimal,
        @Serializable(with = BigDecimalAsStringSerializer::class) val new: BigDecimal
    ) : SerializedAction()

    @Serializable
    data class Transfer(
        val from: Withdraw,
        val to: Deposit
    ) : SerializedAction()
}

@Serializable
data class SerializedTransactionResult(
    val economyId: String,
    val action: SerializedAction
)

object BigDecimalAsStringSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }

    override fun deserialize(decoder: Decoder): BigDecimal = decoder.decodeString().toBigDecimal()
}

fun TransactionResult<Action>.toSerialized(): SerializedTransactionResult? = when (this) {
    is TransactionResult.Success -> SerializedTransactionResult(
        economyId = economyId,
        action = value.toSerializedAction()
    )
    is TransactionResult.Failure -> null
}

fun SerializedTransactionResult.toTransactionResult(): TransactionResult<Action> =
    TransactionResult.success(economyId, action.toApiAction())

private fun Action.toSerializedAction(): SerializedAction = when (this) {
    is Action.Withdraw -> SerializedAction.Withdraw(uuid.toString(), old, new)
    is Action.Deposit -> SerializedAction.Deposit(uuid.toString(), old, new)
    is Action.Set -> SerializedAction.Set(uuid.toString(), old, new)
    is Action.Transfer -> SerializedAction.Transfer(
        from = SerializedAction.Withdraw(from.uuid.toString(), from.old, from.new),
        to = SerializedAction.Deposit(to.uuid.toString(), to.old, to.new)
    )
}

private fun SerializedAction.toApiAction(): Action = when (this) {
    is SerializedAction.Withdraw -> Action.Withdraw(UUID.fromString(uuid), old, new)
    is SerializedAction.Deposit -> Action.Deposit(UUID.fromString(uuid), old, new)
    is SerializedAction.Set -> Action.Set(UUID.fromString(uuid), old, new)
    is SerializedAction.Transfer -> Action.Transfer(
        from = Action.Withdraw(UUID.fromString(from.uuid), from.old, from.new),
        to = Action.Deposit(UUID.fromString(to.uuid), to.old, to.new)
    )
}
