/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

import alakazam.kotlin.core.parse
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.datetime.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive

@Immutable
@Serializable(DbMetadataSerializer::class)
data class DbMetadata(
  val data: PersistentMap<Key<*>, Any?> = persistentMapOf(),
) : Iterable<Map.Entry<DbMetadata.Key<*>, Any?>> {
  constructor(vararg data: Pair<Key<*>, Any?>) : this(data.toMap().toPersistentMap())

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

  @Suppress("UNCHECKED_CAST")
  operator fun <T : Any> get(key: Key<T>): T? = data[key] as? T
  operator fun <T : Any> set(key: Key<T>, value: T?) = DbMetadata(data.put(key, value))
  operator fun plus(other: Map<Key<*>, Any?>) = DbMetadata(data.putAll(other))
  operator fun plus(other: Pair<Key<*>, Any?>) = DbMetadata(data.put(other.first, other.second))
  operator fun minus(key: Key<*>) = DbMetadata(data.remove(key))
  operator fun minus(keys: Collection<Key<*>>) = DbMetadata(data.minus(keys).toPersistentMap())
  override fun iterator() = data.iterator()

  sealed interface Key<T : Any> {
    val name: String
  }

  sealed interface PrimitiveKey<T : Any> : Key<T> {
    fun decode(element: JsonPrimitive): T
  }

  @JvmInline
  value class BoolKey(override val name: String) : PrimitiveKey<Boolean> {
    override fun decode(element: JsonPrimitive) = element.boolean
  }

  @JvmInline
  value class StringKey(override val name: String) : PrimitiveKey<String> {
    override fun decode(element: JsonPrimitive) = element.content
  }

  class TypedKey<T : Any>(override val name: String, private val fromString: (String) -> T) : PrimitiveKey<T> {
    override fun decode(element: JsonPrimitive) = fromString(element.content)
  }

  class ListKey(override val name: String) : Key<List<String>> {
    fun decode(array: JsonArray) = array.map { element -> element.jsonPrimitive.content }
  }

  companion object {
    val BudgetName: Key<String> = StringKey("budgetName")
    val BudgetCollapsed: Key<List<String>> = ListKey("budget.collapsed")
    val CloudFileId: Key<BudgetId> = TypedKey("cloudFileId", ::BudgetId)
    val GroupId: Key<String> = StringKey("groupId")
    val Id: Key<String> = StringKey("id")
    val LastScheduleRun: Key<LocalDate> = TypedKey("lastScheduleRun", LocalDate::parse)
    val LastSyncedTimestamp: Key<Timestamp> = TypedKey("lastSyncedTimestamp", Timestamp::parse)
    val LastUploaded: Key<LocalDate> = TypedKey("lastUploaded", LocalDate::parse)
    val ResetClock: Key<Boolean> = BoolKey("resetClock")
    val UserId: Key<String> = StringKey("userId")
    val EncryptKeyId: Key<String> = StringKey("encryptKeyId")

    fun Key(key: String): Key<*> = when (key) {
      BudgetName.name -> BudgetName
      BudgetCollapsed.name -> BudgetCollapsed
      CloudFileId.name -> CloudFileId
      GroupId.name -> GroupId
      Id.name -> Id
      LastScheduleRun.name -> LastScheduleRun
      LastSyncedTimestamp.name -> LastSyncedTimestamp
      LastUploaded.name -> LastUploaded
      ResetClock.name -> ResetClock
      UserId.name -> UserId
      EncryptKeyId.name -> EncryptKeyId
      else -> StringKey(key)
    }

    inline fun <reified E : Enum<E>> enumKey(name: String): Key<E> = TypedKey(
      name = name,
      fromString = { E::class.parse(it) },
    )
  }
}

val DbMetadata.cloudFileId: BudgetId get() = get(DbMetadata.CloudFileId) ?: error("No cloudFileId found in $this")

@Suppress("UNCHECKED_CAST")
private fun DbMetadata.Key<*>.encode(value: Any?): JsonElement? = when (this) {
  is DbMetadata.BoolKey -> (value as? Boolean)?.let(::JsonPrimitive)
  is DbMetadata.StringKey -> (value as? String)?.let(::JsonPrimitive)
  is DbMetadata.TypedKey<*> -> value?.toString()?.let(::JsonPrimitive)
  is DbMetadata.ListKey -> (value as? List<String>)?.map(::JsonPrimitive)?.let(::JsonArray)
}

private object DbMetadataSerializer : KSerializer<DbMetadata> {
  private val delegate = MapSerializer(String.serializer(), JsonElement.serializer().nullable)
  override val descriptor = delegate.descriptor

  override fun serialize(encoder: Encoder, value: DbMetadata) = delegate.serialize(
    encoder = encoder,
    value = value
      .mapNotNull { (k, v) -> if (v == null) null else k to v }
      .associate { (k, v) -> k.name to k.encode(v) },
  )

  override fun deserialize(decoder: Decoder): DbMetadata {
    val jsonMap = delegate.deserialize(decoder).map { (k, v) ->
      val key = DbMetadata.Key(k)
      key to when (v) {
        is JsonPrimitive -> (key as DbMetadata.PrimitiveKey<*>).decode(v)
        is JsonArray -> (key as DbMetadata.ListKey).decode(v)
        null, is JsonNull -> null
        is JsonObject -> throw SerializationException("Can't decode yet: $v")
      }
    }
    return DbMetadata(jsonMap.toMap().toPersistentMap())
  }
}
