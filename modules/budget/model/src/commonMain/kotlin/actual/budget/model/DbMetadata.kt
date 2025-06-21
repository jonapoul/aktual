package actual.budget.model

import actual.budget.model.DbMetadata.Keys
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
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
import kotlin.reflect.KProperty

@Immutable
@Serializable(DbMetadataSerializer::class)
data class DbMetadata(
  val data: PersistentMap<String, Any?> = persistentMapOf(),
) : Map<String, Any?> by data {
  var budgetName by stringDelegate(Keys.BudgetName)
  var cloudFileId by typedDelegate(Keys.CloudFileId, ::BudgetId)
  var groupId by stringDelegate(Keys.GroupId)
  var id by stringDelegate(Keys.Id)
  var lastScheduleRun by typedDelegate(Keys.LastScheduleRun, LocalDate::parse)
  var lastSyncedTimestamp by typedDelegate(Keys.LastSyncedTimestamp, Timestamp::parse)
  var lastUploaded by typedDelegate(Keys.LastUploaded, LocalDate::parse)
  var resetClock by boolDelegate(Keys.ResetClock)
  var userId by stringDelegate(Keys.UserId)
  var encryptKeyId by primitiveDelegate<String?>(Keys.EncryptKeyId, default = null)

  operator fun plus(other: Map<String, Any?>): DbMetadata = DbMetadata(data.putAll(other))

  operator fun set(key: String, value: Any?): DbMetadata = DbMetadata(data.put(key, value))

  operator fun minus(key: String): DbMetadata = DbMetadata(data.remove(key))

  operator fun minus(keys: Collection<String>): DbMetadata {
    val data = data.toMutableMap()
    for (key in keys) data.remove(key)
    return DbMetadata(data.toPersistentMap())
  }

  val sortColumn = enumDelegate("sortColumn", SortColumn.Default)
  val sortDirection = enumDelegate("sortDirection", SortDirection.Default)
  val transactionFormat = enumDelegate("transactionFormat", TransactionsFormat.Default)

  interface Delegate<T> {
    fun get(): T
    fun set(value: T): DbMetadata
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = get()
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = set(value)
  }

  object Keys {
    const val BudgetName = "budgetName"
    const val CloudFileId = "cloudFileId"
    const val GroupId = "groupId"
    const val Id = "id"
    const val LastScheduleRun = "lastScheduleRun"
    const val LastSyncedTimestamp = "lastSyncedTimestamp"
    const val LastUploaded = "lastUploaded"
    const val ResetClock = "resetClock"
    const val UserId = "userId"
    const val EncryptKeyId = "encryptKeyId"
  }

  private inline fun <reified E : Enum<E>> enumDelegate(key: String, default: E) = object : Delegate<E> {
    override fun set(value: E) = set(key, value.ordinal)
    override fun get(): E = enumValues<E>()[getOrDefault(key, default.ordinal) as Int]
  }

  private inline fun <reified T> primitiveDelegate(key: String, default: T) = object : Delegate<T> {
    override fun set(value: T) = set(key, value)
    override fun get(): T = getOrDefault(key, default) as T
  }

  private fun stringDelegate(key: String) = primitiveDelegate<String>(key)
  private fun boolDelegate(key: String) = primitiveDelegate<Boolean>(key)

  private inline fun <reified T> primitiveDelegate(key: String) = object : Delegate<T> {
    override fun set(value: T) = set(key, value)
    override fun get(): T = requireNotNull(get(key)) as T
  }

  private inline fun <reified T> typedDelegate(
    key: String,
    crossinline decode: (String) -> T,
    crossinline encode: (T) -> String = { it.toString() },
  ) = object : Delegate<T> {
    override fun set(value: T) = set(key, encode(value))
    override fun get(): T = decode(requireNotNull(get(key)) as String)
  }
}

fun DbMetadata(
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
): DbMetadata = DbMetadata(
  persistentMapOf<String, Any?>(
    Keys.BudgetName to budgetName,
    Keys.CloudFileId to cloudFileId.value,
    Keys.GroupId to groupId,
    Keys.Id to id,
    Keys.LastScheduleRun to lastScheduleRun.toString(),
    Keys.LastSyncedTimestamp to lastSyncedTimestamp.toString(),
    Keys.LastUploaded to lastUploaded.toString(),
    Keys.ResetClock to resetClock,
    Keys.UserId to userId,
    Keys.EncryptKeyId to encryptKeyId,
  ),
)

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
    val metadata = mutableMapOf<String, Any?>()
    for ((key, element) in jsonObject) {
      metadata[key] = element.toRealData()
    }
    return DbMetadata(metadata.toPersistentMap())
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
