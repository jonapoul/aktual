package aktual.budget.model

import java.util.stream.IntStream
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

data class SyncResponse(
    val messages: List<MessageEnvelope>,
    val merkle: Merkle,
)

class Merkle(private val encoded: String) :
    Lazy<JsonObject> by lazyMerkle(encoded), CharSequence by encoded {
  override fun chars(): IntStream = encoded.chars()

  override fun codePoints(): IntStream = encoded.codePoints()
}

private fun lazyMerkle(string: String) = lazy {
  Json.decodeFromString(JsonObject.serializer(), string)
}

data class MessageEnvelope(
    val timestamp: Timestamp,
    val isEncrypted: Boolean,
    val content: Message,
)

data class Message(
    val dataset: String,
    val row: String,
    val column: String,
    val timestamp: Timestamp,
    val value: MessageValue,
)

sealed interface MessageValue {
  @JvmInline value class Number(val value: Long) : MessageValue

  @JvmInline value class String(val value: kotlin.String) : MessageValue

  data object Null : MessageValue

  fun encode(): kotlin.String =
      when (this) {
        is String -> "S:$value"
        is Number -> "N:$value"
        Null -> "0"
      }

  companion object {
    fun decode(value: kotlin.String): MessageValue =
        when (val type = value[0]) {
          'N' -> Number(value.substring(startIndex = 2).toLong())
          'S' -> String(value.substring(startIndex = 2))
          '0' -> Null
          else -> throw IllegalArgumentException("Unexpected type '$type' for content '$value'")
        }
  }
}
