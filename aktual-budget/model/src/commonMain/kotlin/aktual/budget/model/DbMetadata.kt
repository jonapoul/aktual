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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

/**
 * Raw JSON values keyed by name. Values are only decoded when read with a [DbMetadataKey], so keys
 * defined outside this class decode as their real type, and unknown keys are kept untouched.
 */
@Serializable(DbMetadata.Serializer::class)
data class DbMetadata(val data: PersistentMap<String, JsonElement> = persistentMapOf()) {
  constructor(
    vararg data: Pair<DbMetadataKey<*>, Any?>,
  ) : this(persistentMapOf<String, JsonElement>() + data)

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
    DbMetadataKey.BudgetName to budgetName,
    DbMetadataKey.BudgetCollapsed to budgetCollapsed,
    DbMetadataKey.CloudFileId to cloudFileId,
    DbMetadataKey.GroupId to groupId,
    DbMetadataKey.Id to id,
    DbMetadataKey.LastScheduleRun to lastScheduleRun,
    DbMetadataKey.LastSyncedTimestamp to lastSyncedTimestamp,
    DbMetadataKey.LastUploaded to lastUploaded,
    DbMetadataKey.ResetClock to resetClock,
    DbMetadataKey.UserId to userId,
    DbMetadataKey.EncryptKeyId to encryptKeyId,
  )

  /** Returns null if the key is missing or its value can't be decoded as [T]. */
  operator fun <T : Any> get(key: DbMetadataKey<T>): T? =
    data[key.name]?.let { element -> runCatching { key.decode(element) }.getOrNull() }

  operator fun <T : Any> set(key: DbMetadataKey<T>, value: T?): DbMetadata =
    DbMetadata(
      if (value == null) data.removing(key.name) else data.putting(key.name, key.encode(value)),
    )

  operator fun plus(other: DbMetadata): DbMetadata = DbMetadata(data.puttingAll(other.data))

  operator fun plus(other: Map<DbMetadataKey<*>, Any?>): DbMetadata = plus(other.toList())

  operator fun plus(other: Iterable<Pair<DbMetadataKey<*>, Any?>>): DbMetadata =
    DbMetadata(data + other)

  operator fun plus(other: Pair<DbMetadataKey<*>, Any?>): DbMetadata = plus(listOf(other))

  operator fun minus(key: DbMetadataKey<*>): DbMetadata = DbMetadata(data.removing(key.name))

  operator fun minus(keys: Collection<DbMetadataKey<*>>): DbMetadata =
    DbMetadata(data.minus(keys.map { it.name }))

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
  pairs: Iterable<Pair<DbMetadataKey<*>, Any?>>,
): PersistentMap<String, JsonElement> = mutate { map ->
  for ((key, value) in pairs) {
    if (value == null) map.remove(key.name) else map[key.name] = key.encodeAny(value)
  }
}

private operator fun PersistentMap<String, JsonElement>.plus(
  pairs: Array<out Pair<DbMetadataKey<*>, Any?>>,
): PersistentMap<String, JsonElement> = plus(pairs.asIterable())

@Suppress("UNCHECKED_CAST")
private fun DbMetadataKey<*>.encodeAny(value: Any): JsonElement =
  (this as DbMetadataKey<Any>).encode(value)
