/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.budget.model

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

@Immutable
@Serializable(DbMetadataSerializer::class)
data class DbMetadata(
  val data: PersistentMap<String, Any?> = persistentMapOf(),
) : Map<String, Any?> by data {
  operator fun <T> get(delegate: Delegate<T>): T = delegate.get(this)
  operator fun set(key: String, value: Any?): DbMetadata = DbMetadata(data.put(key, value))
  operator fun <T> set(delegate: Delegate<T>, value: T): DbMetadata = delegate.set(this, value)
  operator fun plus(other: Map<String, Any?>): DbMetadata = DbMetadata(data.putAll(other))
  operator fun minus(key: String): DbMetadata = DbMetadata(data.remove(key))

  operator fun minus(keys: Collection<String>): DbMetadata {
    val data = data.toMutableMap()
    for (key in keys) data.remove(key)
    return DbMetadata(data.toPersistentMap())
  }

  val cloudFileId: BudgetId get() = get(CloudFileId)

  interface Delegate<T> {
    val key: String
    fun get(meta: DbMetadata): T
    fun set(meta: DbMetadata, value: T): DbMetadata
  }

  companion object {
    val BudgetName = stringDelegate(Keys.BudgetName)
    val CloudFileId = typedDelegate(Keys.CloudFileId, ::BudgetId)
    val GroupId = stringDelegate(Keys.GroupId)
    val Id = stringDelegate(Keys.Id)
    val LastScheduleRun = typedDelegate(Keys.LastScheduleRun, LocalDate::parse)
    val LastSyncedTimestamp = typedDelegate(Keys.LastSyncedTimestamp, Timestamp::parse)
    val LastUploaded = typedDelegate(Keys.LastUploaded, LocalDate::parse)
    val ResetClock = boolDelegate(Keys.ResetClock)
    val UserId = stringDelegate(Keys.UserId)
    val EncryptKeyId = primitiveDelegate<String?>(Keys.EncryptKeyId, default = null)

    inline fun <reified E : Enum<E>> enumDelegate(key: String, default: E) = object : Delegate<E> {
      override val key = key
      override fun set(meta: DbMetadata, value: E) = meta.set(key, value.ordinal)
      override fun get(meta: DbMetadata): E = enumValues<E>()[meta.getOrDefault(key, default.ordinal) as Int]
    }

    private inline fun <reified T> primitiveDelegate(key: String, default: T) = object : Delegate<T> {
      override val key = key
      override fun set(meta: DbMetadata, value: T) = meta.set(key, value)
      override fun get(meta: DbMetadata): T = meta.getOrDefault(key, default) as T
    }

    private fun stringDelegate(key: String) = primitiveDelegate<String>(key)
    private fun boolDelegate(key: String) = primitiveDelegate<Boolean>(key)

    private inline fun <reified T> primitiveDelegate(key: String) = object : Delegate<T> {
      override val key = key
      override fun set(meta: DbMetadata, value: T) = meta.set(key, value)
      override fun get(meta: DbMetadata): T = requireNotNull(meta[key]) as T
    }

    private inline fun <reified T> typedDelegate(
      key: String,
      crossinline decode: (String) -> T,
      crossinline encode: (T) -> String = { it.toString() },
    ) = object : Delegate<T> {
      override val key = key
      override fun set(meta: DbMetadata, value: T) = meta.set(key, encode(value))
      override fun get(meta: DbMetadata): T = decode(requireNotNull(meta[key]) as String)
    }
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

private object Keys {
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
