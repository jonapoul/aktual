package aktual.budget.model

import java.math.BigDecimal
import java.util.stream.IntStream
import kotlin.String as KString
import kotlin.math.abs
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

data class SyncResponse(val messages: List<MessageEnvelope>, val merkle: Merkle)

data class Merkle(private val encoded: KString) :
  Lazy<JsonObject> by lazyMerkle(encoded), CharSequence by encoded {
  override fun chars(): IntStream = encoded.chars()

  override fun codePoints(): IntStream = encoded.codePoints()

  override fun toString(): KString = encoded
}

private fun lazyMerkle(string: KString) = lazy {
  Json.decodeFromString(JsonObject.serializer(), string)
}

data class MessageEnvelope(val timestamp: Timestamp, val isEncrypted: Boolean, val content: Message)

data class Message(
  val dataset: KString,
  val row: KString,
  val column: KString,
  val timestamp: Timestamp,
  val value: MessageValue,
)

fun KString?.messageValue(): MessageValue = if (this == null) Null else MessageValue.String(this)

fun Boolean?.messageValue(): MessageValue =
  when (this) {
    true -> MessageValue.Number(1)
    false -> MessageValue.Number(0)
    null -> Null
  }

// Dates are stored as integers like 20261001, see toDateRepr() in loot-core
@Suppress("MagicNumber")
fun LocalDate?.messageValue(): MessageValue =
  if (this == null) Null else MessageValue.Number(year * 10_000L + month.number * 100L + day)

sealed interface MessageValue {
  @JvmInline value class Number(val value: Long) : MessageValue

  @JvmInline value class Real(val value: Double) : MessageValue

  @JvmInline value class String(val value: KString) : MessageValue

  data object Null : MessageValue

  fun encode(): KString =
    when (this) {
      is String -> "S:$value"
      is Number -> "N:$value"
      is Real -> "N:${value.toJsString()}"
      Null -> "0:"
    }

  companion object {
    fun decode(value: KString): MessageValue =
      when (val type = value[0]) {
        'N' -> decodeNumber(value.substring(startIndex = 2))
        'S' -> String(value.substring(startIndex = 2))
        '0' -> Null
        else -> throw IllegalArgumentException("Unexpected type '$type' for content '$value'")
      }
  }
}

private fun decodeNumber(value: KString): MessageValue =
  value.toLongOrNull()?.let(MessageValue::Number) ?: MessageValue.Real(value.toDouble())

// As JS's Number.prototype.toString, which upstream's serializeValue uses
@Suppress("MagicNumber")
private fun Double.toJsString(): KString =
  when {
    isNaN() -> "NaN"
    isInfinite() -> if (this > 0) "Infinity" else "-Infinity"
    this == 0.0 -> "0"
    abs(this) in 1e-6..<1e21 -> BigDecimal.valueOf(this).stripTrailingZeros().toPlainString()
    else -> toString().lowercase().replace(".0e", "e").replace(JS_EXPONENT, "e+$1")
  }

private val JS_EXPONENT = Regex("e(\\d)")
