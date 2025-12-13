package aktual.budget.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.util.stream.IntStream

data class SyncResponse(
  val messages: List<MessageEnvelope>,
  val merkle: Merkle,
)

class Merkle(private val encoded: String) : Lazy<JsonObject> by lazyMerkle(encoded), CharSequence by encoded {
  override fun chars(): IntStream = encoded.chars()
  override fun codePoints(): IntStream = encoded.codePoints()
}

private fun lazyMerkle(string: String) = lazy { Json.decodeFromString(JsonObject.serializer(), string) }

data class MessageEnvelope(
  val timestamp: Timestamp,
  val isEncrypted: Boolean,
  val content: Message,
)

data class Message(
  val dataset: String,
  val row: String,
  val column: String,
  val value: MessageValue,
)

sealed interface MessageValue {
  @JvmInline
  value class Number(val value: Float) : MessageValue

  @JvmInline
  value class String(val value: kotlin.String) : MessageValue

  data object Null : MessageValue
}
