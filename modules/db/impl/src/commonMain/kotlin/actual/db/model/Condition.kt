package actual.db.model

import actual.budget.model.ConditionOperator
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(ConditionSerializer::class)
data class Condition(
  val field: String,
  val operator: ConditionOperator,
  val type: String,
  val values: List<String>,
)

private object ConditionSerializer : KSerializer<Condition> {
  @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
  override val descriptor = buildSerialDescriptor("ConditionsSerializer", PolymorphicKind.SEALED)

  override fun serialize(encoder: Encoder, value: Condition) =
    throw SerializationException("Serialization not supported! value=$value")

  private fun JsonObject.getOrThrow(key: String): JsonElement =
    get(key) ?: throw SerializationException("No $key in $this")

  private val JsonElement.string: String
    get() = with(jsonPrimitive) { if (isString) content else error("Not a string: $this") }

  override fun deserialize(decoder: Decoder): Condition {
    val input = decoder as? JsonDecoder ?: error("Need JsonDecoder, got $decoder")
    val jsonObject = input.decodeJsonElement().jsonObject
    val field = jsonObject.getOrThrow(key = "field").string
    val operator = jsonObject.getOrThrow(key = "op").string.let(ConditionOperator::fromString)
    val type = jsonObject.getOrThrow(key = "type").string
    return when (val value = jsonObject.getOrThrow(key = "value")) {
      is JsonPrimitive -> Condition(field, operator, type, listOf(value.string))
      is JsonArray -> Condition(field, operator, type, value.map { it.string })
      else -> throw SerializationException("Unexpected value $value in $jsonObject")
    }
  }
}
