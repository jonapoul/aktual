package aktual.budget.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/** Keys are equal by [name] alone, so separately-created keys find the same value. */
@Suppress("UseDataClass") // a data class would compare the lambdas too
class DbMetadataKey<T : Any>(
  val name: String,
  val decode: (JsonElement) -> T,
  val encode: (T) -> JsonElement,
) {
  override fun equals(other: Any?) = other is DbMetadataKey<*> && other.name == name

  override fun hashCode() = name.hashCode()

  override fun toString() = "DbMetadataKey($name)"

  companion object {
    val BudgetName = stringKey("budgetName")
    val BudgetCollapsed = listKey("budget.collapsed")
    val BudgetShowHiddenCategories = boolKey("budget.showHiddenCategories")
    val BudgetMonthCount = intKey("budget.monthCount")
    val MobileShowSpentColumn = boolKey("mobile.showSpentColumn")
    val HomeCardOrder = listKey("home.cardOrder")
    val HomeReports = listKey("home.reports")
    val CloudFileId = typedKey("cloudFileId", ::BudgetId)
    val GroupId = stringKey("groupId")
    val Id = stringKey("id")
    val LastScheduleRun = typedKey("lastScheduleRun", LocalDate::parse)
    val LastSyncedTimestamp = typedKey("lastSyncedTimestamp", Timestamp::parse)
    val LastUploaded = typedKey("lastUploaded", LocalDate::parse)
    val ResetClock = boolKey("resetClock")
    val UserId = stringKey("userId")
    val EncryptKeyId = stringKey("encryptKeyId")
    val TransactionDensity = serializableKey<TransactionsDensity>("transactionDensity")

    fun boolKey(name: String): DbMetadataKey<Boolean> =
      DbMetadataKey(name, decode = { it.jsonPrimitive.boolean }, encode = { JsonPrimitive(it) })

    fun intKey(name: String): DbMetadataKey<Int> =
      DbMetadataKey(name, decode = { it.jsonPrimitive.int }, encode = { JsonPrimitive(it) })

    fun stringKey(name: String): DbMetadataKey<String> =
      DbMetadataKey(name, decode = { it.jsonPrimitive.content }, encode = { JsonPrimitive(it) })

    fun listKey(name: String): DbMetadataKey<List<String>> =
      DbMetadataKey(
        name,
        decode = { it.jsonArray.map { element -> element.jsonPrimitive.content } },
        encode = { JsonArray(it.map(::JsonPrimitive)) },
      )

    /** Written with [toString], read with [fromString]. */
    fun <T : Any> typedKey(name: String, fromString: (String) -> T): DbMetadataKey<T> =
      DbMetadataKey(
        name,
        decode = { fromString(it.jsonPrimitive.content) },
        encode = { JsonPrimitive(it.toString()) },
      )

    inline fun <reified T : Any> serializableKey(name: String): DbMetadataKey<T> =
      DbMetadataKey(
        name,
        decode = { Json.decodeFromJsonElement<T>(it) },
        encode = { Json.encodeToJsonElement(it) },
      )
  }
}
