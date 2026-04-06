package aktual.budget.model

import java.util.stream.IntStream
import kotlin.String as KString
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

sealed interface MessageValue {
  @JvmInline value class Number(val value: Long) : MessageValue

  @JvmInline value class String(val value: KString) : MessageValue

  data object Null : MessageValue

  fun encode(): KString =
    when (this) {
      is String -> "S:$value"
      is Number -> "N:$value"
      Null -> "0:"
    }

  companion object {
    fun decode(value: KString): MessageValue =
      when (val type = value[0]) {
        'N' -> Number(value.substring(startIndex = 2).toLong())
        'S' -> String(value.substring(startIndex = 2))
        '0' -> Null
        else -> throw IllegalArgumentException("Unexpected type '$type' for content '$value'")
      }
  }
}
