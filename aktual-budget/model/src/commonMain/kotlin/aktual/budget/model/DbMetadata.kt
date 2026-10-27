package aktual.budget.model

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.minus
import kotlinx.collections.immutable.mutate
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * Raw JSON values keyed by name. Values are only decoded when read with a [Key], so keys defined
 * outside this class decode as their real type, and unknown keys are kept untouched.
 */
@Serializable(DbMetadata.Serializer::class)
data class DbMetadata(val data: PersistentMap<String, JsonElement> = persistentMapOf()) {
  constructor(vararg data: Pair<Key<*>, Any?>) : this(persistentMapOf<String, JsonElement>() + data)

  constructor(
    budgetName: String? = null,
    budgetCollapsed: List<String>? = null,
    cloudFileId: BudgetId? = null,
    groupId: String? = null,
    id: String? = null,
    lastScheduleRun: LocalDate? = null,
    lastSyncedTimestamp: Timestamp? = null,
    lastUploaded: LocalDate? = null,
    resetClock: Boolean? = null,
    userId: String? = null,
    encryptKeyId: String? = null,
  ) : this(
    BudgetName to budgetName,
    BudgetCollapsed to budgetCollapsed,
    CloudFileId to cloudFileId,
    GroupId to groupId,
    Id to id,
    LastScheduleRun to lastScheduleRun,
    LastSyncedTimestamp to lastSyncedTimestamp,
    LastUploaded to lastUploaded,
    ResetClock to resetClock,
    UserId to userId,
    EncryptKeyId to encryptKeyId,
  )

  /** Returns null if the key is missing or its value can't be decoded as [T]. */
  operator fun <T : Any> get(key: Key<T>): T? =
    data[key.name]?.let { element -> runCatching { key.decode(element) }.getOrNull() }

  operator fun <T : Any> set(key: Key<T>, value: T?): DbMetadata =
    DbMetadata(
      if (value == null) data.removing(key.name) else data.putting(key.name, key.encode(value)),
    )

  operator fun plus(other: DbMetadata): DbMetadata = DbMetadata(data.puttingAll(other.data))

  operator fun plus(other: Map<Key<*>, Any?>): DbMetadata = plus(other.toList())

  operator fun plus(other: Iterable<Pair<Key<*>, Any?>>): DbMetadata = DbMetadata(data + other)

  operator fun plus(other: Pair<Key<*>, Any?>): DbMetadata = plus(listOf(other))

  operator fun minus(key: Key<*>): DbMetadata = DbMetadata(data.removing(key.name))

  operator fun minus(keys: Collection<Key<*>>): DbMetadata =
    DbMetadata(data.minus(keys.map { it.name }))

  /** Keys are equal by [name] alone, so separately-created keys find the same value. */
  @Suppress("UseDataClass") // a data class would compare the lambdas too
  class Key<T : Any>(
    val name: String,
    val decode: (JsonElement) -> T,
    val encode: (T) -> JsonElement,
  ) {
    override fun equals(other: Any?) = other is Key<*> && other.name == name

    override fun hashCode() = name.hashCode()

    override fun toString() = "Key($name)"
  }

  companion object {
    val BudgetName: Key<String> = stringKey("budgetName")
    val BudgetCollapsed: Key<List<String>> = listKey("budget.collapsed")
    val BudgetShowHiddenCategories: Key<Boolean> = boolKey("budget.showHiddenCategories")
    val BudgetMonthCount: Key<Int> = intKey("budget.monthCount")
    val MobileShowSpentColumn: Key<Boolean> = boolKey("mobile.showSpentColumn")
    val HomeCardOrder: Key<List<String>> = listKey("home.cardOrder")
    val HomeReports: Key<List<String>> = listKey("home.reports")
    val CloudFileId: Key<BudgetId> = typedKey("cloudFileId", ::BudgetId)
    val GroupId: Key<String> = stringKey("groupId")
    val Id: Key<String> = stringKey("id")
    val LastScheduleRun: Key<LocalDate> = typedKey("lastScheduleRun", LocalDate::parse)
    val LastSyncedTimestamp: Key<Timestamp> = typedKey("lastSyncedTimestamp", Timestamp::parse)
    val LastUploaded: Key<LocalDate> = typedKey("lastUploaded", LocalDate::parse)
    val ResetClock: Key<Boolean> = boolKey("resetClock")
    val UserId: Key<String> = stringKey("userId")
    val EncryptKeyId: Key<String> = stringKey("encryptKeyId")
    val TransactionDensity: Key<TransactionsDensity> = enumKey("transactionDensity")

    fun boolKey(name: String): Key<Boolean> =
      Key(name, decode = { it.jsonPrimitive.boolean }, encode = { JsonPrimitive(it) })

    fun intKey(name: String): Key<Int> =
      Key(name, decode = { it.jsonPrimitive.int }, encode = { JsonPrimitive(it) })

    fun stringKey(name: String): Key<String> =
      Key(name, decode = { it.jsonPrimitive.content }, encode = { JsonPrimitive(it) })

    fun listKey(name: String): Key<List<String>> =
      Key(
        name,
        decode = { it.jsonArray.map { element -> element.jsonPrimitive.content } },
        encode = { JsonArray(it.map(::JsonPrimitive)) },
      )

    /** Written with [toString], read with [fromString]. */
    fun <T : Any> typedKey(name: String, fromString: (String) -> T): Key<T> =
      Key(
        name,
        decode = { fromString(it.jsonPrimitive.content) },
        encode = { JsonPrimitive(it.toString()) },
      )

    /** Written and read by [Enum.name]. */
    inline fun <reified E : Enum<E>> enumKey(name: String): Key<E> =
      Key(
        name,
        decode = { element -> enumValues<E>().first { it.name == element.jsonPrimitive.content } },
        encode = { JsonPrimitive(it.name) },
      )
  }

  object Serializer : KSerializer<DbMetadata> {
    private val delegate = MapSerializer(String.serializer(), JsonElement.serializer().nullable)
    override val descriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: DbMetadata) =
      delegate.serialize(encoder, value.data)

    override fun deserialize(decoder: Decoder): DbMetadata {
      val data = delegate.deserialize(decoder).filterValues { it != null && it !is JsonNull }
      @Suppress("UNCHECKED_CAST")
      return DbMetadata((data as Map<String, JsonElement>).toPersistentMap())
    }
  }
}

private operator fun PersistentMap<String, JsonElement>.plus(
  pairs: Iterable<Pair<DbMetadata.Key<*>, Any?>>,
): PersistentMap<String, JsonElement> = mutate { map ->
  for ((key, value) in pairs) {
    if (value == null) map.remove(key.name) else map[key.name] = key.encodeAny(value)
  }
}

private operator fun PersistentMap<String, JsonElement>.plus(
  pairs: Array<out Pair<DbMetadata.Key<*>, Any?>>,
): PersistentMap<String, JsonElement> = plus(pairs.asIterable())

@Suppress("UNCHECKED_CAST")
private fun DbMetadata.Key<*>.encodeAny(value: Any): JsonElement =
  (this as DbMetadata.Key<Any>).encode(value)
