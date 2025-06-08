package actual.budget.model

import alakazam.kotlin.core.deepCopy
import kotlinx.datetime.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

@Serializable(DbMetadataSerializer::class)
data class DbMetadata(
  private val data: MutableMap<String, Any?> = mutableMapOf(),
) : MutableMap<String, Any?> by data {
  constructor(other: DbMetadata) : this(other.deepCopy().toMutableMap())

  constructor(
    budgetName: String,
    cloudFileId: BudgetId,
    groupId: String,
    id: String,
    lastScheduleRun: LocalDate,
    lastSyncedTimestamp: Timestamp,
    lastUploaded: LocalDate,
    resetClock: Boolean,
    userId: String,
    encryptKeyId: String? = null,
  ) : this() {
    data["budgetName"] = budgetName
    data["cloudFileId"] = cloudFileId.value
    data["groupId"] = groupId
    data["id"] = id
    data["lastScheduleRun"] = lastScheduleRun.toString()
    data["lastSyncedTimestamp"] = lastSyncedTimestamp.toString()
    data["lastUploaded"] = lastUploaded.toString()
    data["resetClock"] = resetClock
    data["userId"] = userId
    data["encryptKeyId"] = encryptKeyId
  }

  var budgetName: String
    get() = data["budgetName"].string
    set(value) = data.set("budgetName", value)

  var cloudFileId: BudgetId
    get() = data["cloudFileId"].string.let(::BudgetId)
    set(value) = data.set("cloudFileId", value.toString())

  var groupId: String
    get() = data["groupId"].string
    set(value) = data.set("groupId", value)

  var id: String
    get() = data["id"].string
    set(value) = data.set("id", value)

  var lastScheduleRun: LocalDate
    get() = data["lastScheduleRun"].string.let(LocalDate::parse)
    set(value) = data.set("lastScheduleRun", value.toString())

  var lastSyncedTimestamp: Timestamp
    get() = data["lastSyncedTimestamp"].string.let(Timestamp::parse)
    set(value) = data.set("lastSyncedTimestamp", value.toString())

  var lastUploaded: LocalDate
    get() = data["lastUploaded"].string.let(LocalDate::parse)
    set(value) = data.set("lastUploaded", value.toString())

  var resetClock: Boolean
    get() = data["resetClock"].string.toBoolean()
    set(value) = data.set("resetClock", value)

  var userId: String
    get() = data["userId"].string
    set(value) = data.set("userId", value)

  var encryptKeyId: String?
    get() = data["encryptKeyId"].string
    set(value) = data.set("encryptKeyId", value)

  companion object {
    private val Any?.string: String get() = this as String
  }
}

private object DbMetadataSerializer : KSerializer<DbMetadata> {
  private val delegate = MapSerializer(String.serializer(), JsonElement.serializer().nullable)
  override val descriptor = delegate.descriptor

  override fun serialize(encoder: Encoder, value: DbMetadata) {
    val content = mutableMapOf<String, JsonElement>()
    value.forEach { (k, v) -> content[k] = v.toJsonElement() }
    (encoder as JsonEncoder).encodeJsonElement(JsonObject(content))
  }

  override fun deserialize(decoder: Decoder): DbMetadata {
    val jsonObject = (decoder as JsonDecoder).decodeJsonElement().jsonObject
    val metadata = DbMetadata()
    for ((key, element) in jsonObject) {
      metadata[key] = element.toRealData()
    }
    return metadata
  }

  private fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is String -> JsonPrimitive(this)
    is Int -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is List<*> -> JsonArray(map { it.toJsonElement() })
    else -> error("Unknown metadata type ${javaClass.canonicalName} = $this")
  }

  private fun JsonElement.toRealData(): Any? = when (this) {
    is JsonNull -> null
    is JsonObject -> error("Can't handle json objects yet: $this")
    is JsonArray -> map { it.toRealData() }
    is JsonPrimitive -> when {
      isString -> content
      else -> content.toBooleanStrictOrNull()
        ?: content.toIntOrNull()
        ?: error("Can't parse primitive content '$content'")
    }
  }
}
